#!/usr/bin/env python3
"""Pack body-only Bellbound poses; the native flail keeps exact combat geometry."""
import hashlib
import json
import re
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageOps

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
from tools.pack_boss_sprites import components

CELL = (112, 112)
ANCHOR = (56, 100)
COLUMNS = ['idle', 'walk_contact_right', 'walk_pass_right', 'walk_contact_left',
           'walk_pass_left', 'windup', 'sweep', 'slam', 'toll_brace', 'fallen']
# Reviewed gauntlet centers in the body-only source. Their packed offsets also
# drive the chain start; the flail endpoint is always calculated by gameplay.
SOURCE_HANDS = [
    [(158, 160), (319, 159), (488, 132), (659, 160), (831, 135),
     (889, 31), (1091, 158), (1286, 207), (1407, 75), (1585, 206)],
    [(40, 380), (223, 372), (380, 365), (546, 373), (732, 369),
     (887, 247), (1102, 367), (1261, 422), (1410, 278), (1750, 423)],
    [(146, 588), (301, 575), (469, 572), (638, 588), (795, 580),
     (973, 457), (1126, 591), (1308, 635), (1527, 486), (1580, 638)],
    [(30, 790), (195, 788), (362, 788), (534, 788), (704, 789),
     (885, 680), (1045, 780), (1217, 822), (1412, 691), (1581, 839)],
]
MIRRORED = {(0, 5), (0, 8)}


def pack_bellbound(source):
    image = Image.open(source).convert('RGBA')
    parts = sorted(components(image), key=lambda pose: pose['box'][3])
    if len(parts) != 40:
        raise ValueError('Expected 40 complete connected body-only poses; found ' + str(len(parts)))
    rows = [sorted(parts[row * 10:(row + 1) * 10], key=lambda pose: pose['box'][0])
            for row in range(4)]
    atlas = Image.new('RGBA', (CELL[0] * 10, CELL[1] * 4))
    pixels = image.load()
    frames = []
    for row, poses in enumerate(rows):
        idle = poses[0]['box']
        scale = 70 / (idle[3] - idle[1])
        for col, pose in enumerate(poses):
            x0, y0, x1, y1 = pose['box']
            tile = Image.new('RGBA', (x1 - x0, y1 - y0))
            target = tile.load()
            for index in pose['points']:
                x, y = index % image.width, index // image.width
                target[x - x0, y - y0] = pixels[x, y][:3] + (255,)
            # The cuirass stays over the hips. A raised fist must never become
            # the top-band alignment reference or move the whole giant sideways.
            chest = [index % image.width for index in pose['points']
                     if y1 - (idle[3] - idle[1]) * 0.75 <= index // image.width <
                     y1 - (idle[3] - idle[1]) * 0.45]
            axis = round(sum(chest) / len(chest)) - x0 if col != 9 else (x1 - x0 - 1) // 2
            hand_x, hand_y = SOURCE_HANDS[row][col]
            if not (x0 <= hand_x < x1 and y0 <= hand_y < y1) or pixels[hand_x, hand_y][3] < 128:
                raise ValueError('Reviewed hand is outside its gauntlet: ' + str((row, col)))
            hand_x -= x0
            hand_y -= y0
            if (row, col) in MIRRORED:
                tile = ImageOps.mirror(tile)
                axis = tile.width - 1 - axis
                hand_x = tile.width - 1 - hand_x
            tile = tile.resize((max(1, round(tile.width * scale)), max(1, round(tile.height * scale))),
                               Image.Resampling.NEAREST)
            bounds = tile.getchannel('A').getbbox()
            left = ANCHOR[0] - round(axis * scale)
            top = ANCHOR[1] - (bounds[3] - 1)
            packed_bounds = [left + bounds[0], top + bounds[1], left + bounds[2], top + bounds[3]]
            if packed_bounds[0] < 1 or packed_bounds[2] > CELL[0] - 1 or packed_bounds[1] < 1 or packed_bounds[3] > CELL[1] - 1:
                raise ValueError('Clipped Bellbound pose: ' + str((row, col)) + ' ' + str(packed_bounds))
            atlas.paste(tile, (col * CELL[0] + left, row * CELL[1] + top), tile)
            grip = [left + round(hand_x * scale) - ANCHOR[0],
                    top + round(hand_y * scale) - ANCHOR[1]]
            frames.append({'row': row, 'column': col, 'source_box': pose['box'],
                           'source_axis': axis, 'scale': scale,
                           'source_hand': list(SOURCE_HANDS[row][col]),
                           'mirror': (row, col) in MIRRORED, 'hand_offset': grip,
                           'packed_visible_bounds': packed_bounds})
    return atlas, frames


def main():
    source = ROOT / 'assets/belfry-sheets/bellbound_body.png'
    atlas, frames = pack_bellbound(source)
    atlas.save(ROOT / 'res/bellbound-atlas.png', optimize=True)
    metadata = {'source': source.name, 'source_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
                'cell_size': list(CELL), 'anchor': list(ANCHOR),
                'directions': ['down', 'left', 'right', 'up'], 'columns': COLUMNS,
                'weapon': 'Native BellboundArt chain and bell remain tied to combat geometry',
                'frames': frames}
    (source.parent / 'bellbound-frames.json').write_text(json.dumps(metadata, indent=2) + '\n', encoding='utf-8')
    java = ROOT / 'src/BellboundSprites.java'
    content = java.read_text(encoding='utf-8')
    hands = '\n'.join('    private static final int[] GRIP_' + axis + '={' +
                      ','.join(str(frame['hand_offset'][index]) for frame in frames) + '};'
                      for index, axis in enumerate(('X', 'Y')))
    content, replacements = re.subn(r'    // BEGIN PACKED GRIPS.*?    // END PACKED GRIPS',
                                   '    // BEGIN PACKED GRIPS\n' + hands + '\n    // END PACKED GRIPS',
                                   content, flags=re.DOTALL)
    if replacements != 1:
        raise ValueError('BellboundSprites packed hand markers are missing')
    java.write_text(content, encoding='utf-8')
    contact = Image.new('RGB', atlas.size, (18, 23, 32))
    contact.paste(atlas, (0, 0), atlas)
    draw = ImageDraw.Draw(contact)
    for x in range(CELL[0], atlas.width, CELL[0]):
        draw.line((x, 0, x, atlas.height - 1), fill=(45, 51, 64))
    for y in range(CELL[1], atlas.height, CELL[1]):
        draw.line((0, y, atlas.width - 1, y), fill=(45, 51, 64))
    (ROOT / 'preview').mkdir(exist_ok=True)
    contact.resize((atlas.width * 2, atlas.height * 2), Image.Resampling.NEAREST).save(ROOT / 'preview/bellbound-packing-check.png')
    print('Packed 40 body-only Bellbound frames:', atlas.size)


if __name__ == '__main__':
    main()
