#!/usr/bin/env python3
"""Pack four sit-down sequences with binary MIDP alpha and stable ground anchors."""
import hashlib
import json
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
from tools.pack_boss_sprites import components

CELL = (80, 64)
ANCHOR = (40, 52)


def pack_rest(source, idle_atlas):
    image = Image.open(source).convert('RGBA')
    parts = components(image)
    rows = [[] for _ in range(4)]
    for pose in parts:
        # Seated figures get shorter, but their grounded soles remain in the same row.
        row = min(3, max(0, round((pose['box'][3] - 260) / 248)))
        rows[row].append(pose)
    if len(parts) != 24 or any(len(row) != 6 for row in rows):
        raise ValueError('Expected 24 complete connected sprites: six per direction')
    for row in rows:
        row.sort(key=lambda pose: pose['box'][0])
    atlas = Image.new('RGBA', (480, 256))
    metadata = []
    pixels = image.load()
    for row in range(4):
        standing = rows[row][0]['box']
        scale = 44 / (standing[3] - standing[1])
        for col, pose in enumerate(rows[row]):
            x0, y0, x1, y1 = pose['box']
            # A head/body axis avoids shifting the hips because the sword gets wider.
            head = [index % image.width for index in pose['points']
                    if index // image.width < y0 + 25]
            axis = round(sum(head) / len(head)) - x0
            tile = Image.new('RGBA', (x1 - x0, y1 - y0))
            target = tile.load()
            for index in pose['points']:
                x, y = index % image.width, index // image.width
                target[x - x0, y - y0] = pixels[x, y][:3] + (255,)
            tile = tile.resize((round(tile.width * scale), round(tile.height * scale)),
                               Image.Resampling.NEAREST)
            bounds = tile.getchannel('A').getbbox()
            left = ANCHOR[0] - round(axis * scale)
            top = ANCHOR[1] - (bounds[3] - 1)
            if left + bounds[0] < 1 or left + bounds[2] > 79 or top + bounds[1] < 1 or top + bounds[3] > 63:
                raise ValueError('Clipped resting sprite: ' + str((row, col)))
            atlas.paste(tile, (col * 80 + left, row * 64 + top), tile)
            # Enter/leave rest with precisely the same standing pixels as the game.
            if col == 0:
                atlas.paste(idle_atlas.crop((0, row * 64, 80, (row + 1) * 64)),
                            (0, row * 64))
            packed = atlas.crop((col * 80, row * 64, (col + 1) * 80, (row + 1) * 64))
            metadata.append({'row': row, 'column': col, 'source_box': pose['box'],
                             'source_axis': axis, 'scale': scale,
                             'standing_from_idle_atlas': col == 0,
                             'packed_visible_bounds': list(packed.getchannel('A').getbbox())})
    return atlas, metadata


def main():
    source = ROOT / 'assets/knight-sheets/knight_rest.png'
    idle_path = ROOT / 'res/hero-atlas.png'
    atlas, frames = pack_rest(source, Image.open(idle_path).convert('RGBA'))
    atlas.save(ROOT / 'res/hero-rest-atlas.png', optimize=True)
    metadata = {'source': source.name,
                'source_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
                'standing_reference': 'res/hero-atlas.png',
                'standing_reference_sha256': hashlib.sha256(idle_path.read_bytes()).hexdigest(),
                'cell_size': list(CELL), 'anchor': list(ANCHOR),
                'directions': ['down', 'left', 'right', 'up'],
                'columns': ['standing', 'lowering', 'crouching', 'sitting_down',
                            'seated_inhale', 'seated_exhale'], 'frames': frames}
    (source.parent / 'rest-frames.json').write_text(json.dumps(metadata, indent=2) + '\n', encoding='utf-8')
    # Contact sheet is only a review aid; the game consumes the transparent atlas.
    contact = Image.new('RGB', atlas.size, (18, 23, 32))
    contact.paste(atlas, (0, 0), atlas)
    draw = ImageDraw.Draw(contact)
    for x in range(80, 480, 80):
        draw.line((x, 0, x, 255), fill=(45, 51, 64))
    for y in range(64, 256, 64):
        draw.line((0, y, 479, y), fill=(45, 51, 64))
    (ROOT / 'preview').mkdir(exist_ok=True)
    contact.resize((960, 512), Image.Resampling.NEAREST).save(ROOT / 'preview/rest-packing-check.png')
    print('Packed 24 rest frames:', atlas.size)


if __name__ == '__main__':
    main()
