#!/usr/bin/env python3
"""Cut isolated generated boss poses, align soles, and pack binary MIDP alpha."""
import hashlib
import json
from collections import deque
from pathlib import Path
from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
CELL = (224, 112)
ANCHOR = (112, 100)

def components(image):
    width, height = image.size
    active = bytearray(value >= 128 for value in image.getchannel('A').tobytes())
    result = []
    for start in range(width * height):
        if not active[start]:
            continue
        active[start] = 0
        queue = deque([start])
        points = []
        while queue:
            index = queue.popleft()
            points.append(index)
            x, y = index % width, index // width
            for yy in range(max(0, y - 1), min(height, y + 2)):
                for xx in range(max(0, x - 1), min(width, x + 2)):
                    neighbour = yy * width + xx
                    if active[neighbour]:
                        active[neighbour] = 0
                        queue.append(neighbour)
        if len(points) >= 1000:
            xs = [p % width for p in points]
            ys = [p // width for p in points]
            result.append({'box': [min(xs), min(ys), max(xs) + 1, max(ys) + 1],
                           'points': points})
    return result

def source_rows(image):
    poses = components(image)
    rows = [[] for _ in range(4)]
    for pose in poses:
        row = round((pose['box'][3] - 215) / 216)
        if not 0 <= row < 4:
            raise ValueError('Unexpected source row')
        rows[row].append(pose)
    if any(len(row) != 8 for row in rows):
        raise ValueError('Expected eight separate complete poses in each row')
    for row in rows:
        row.sort(key=lambda pose: pose['box'][0])
    return rows

def main():
    source = ROOT / 'assets/boss-sheets/boss_skeleton.png'
    fixed_source = source.with_name('boss_skeleton_walk_fixed.png')
    image = Image.open(source).convert('RGBA')
    fixed_image = Image.open(fixed_source).convert('RGBA')
    rows = source_rows(image)
    fixed_rows = source_rows(fixed_image)
    fixed_poses = {(0, 2), (0, 3), (1, 2), (1, 4), (2, 2), (2, 4), (3, 2)}
    atlas = Image.new('RGBA', (CELL[0] * 8, CELL[1] * 4))
    frames = []
    for row in range(4):
        idle = rows[row][0]['box']
        scale = 88 / (idle[3] - idle[1])
        for col in range(8):
            # Generated left windup/strike face right: use the matching right
            # profiles reflected during extraction rather than play backward.
            sr = 2 if row == 1 and col in (5, 6) else row
            mirror = sr != row
            use_fixed = (row, col) in fixed_poses
            frame_image = fixed_image if use_fixed else image
            pixels = frame_image.load()
            pose = fixed_rows[sr][col] if use_fixed else rows[sr][col]
            x0, y0, x1, y1 = pose['box']
            tile = Image.new('RGBA', (x1 - x0, y1 - y0))
            target = tile.load()
            for index in pose['points']:
                x, y = index % frame_image.width, index // frame_image.width
                target[x - x0, y - y0] = pixels[x, y][:3] + (255,)
            # Anchor to the head/body axis; a sword never determines the center.
            if col < 7:
                ivory = []
                for index in pose['points']:
                    xx, yy = index % frame_image.width, index // frame_image.width
                    red, green, blue, alpha = pixels[xx, yy]
                    if yy < y0 + 75 and red >= 170 and green >= 145 and blue >= 105 and 4 <= red - green <= 45 and 8 <= green - blue <= 65:
                        ivory.append((xx, yy))
                if not ivory:
                    raise ValueError('Missing skull for anchor: ' + str((row, col)))
                skull_top = min(yy for xx, yy in ivory)
                head = [xx for xx, yy in ivory if yy < skull_top + 25]
                axis = round(sum(head) / len(head)) - x0
            else:
                axis = tile.width // 2
            if mirror:
                tile = ImageOps.mirror(tile)
                axis = tile.width - 1 - axis
            size = (round(tile.width * scale), round(tile.height * scale))
            tile = tile.resize(size, Image.Resampling.NEAREST)
            # Binary alpha also excludes isolated resampling dust.
            visible = tile.getchannel('A').getbbox()
            left = ANCHOR[0] - round(axis * scale)
            top = ANCHOR[1] - (visible[3] - 1)
            if left + visible[0] <= 0 or left + visible[2] >= CELL[0] or top + visible[1] <= 0 or top + visible[3] >= CELL[1]:
                raise ValueError('Clipped boss frame: ' + str((row, col)))
            atlas.paste(tile, (col * CELL[0] + left, row * CELL[1] + top), tile)
            frames.append({'row': row, 'column': col, 'source_row': sr,
                           'source_filename': fixed_source.name if use_fixed else source.name,
                           'source_box': pose['box'], 'mirror': mirror,
                           'scale': scale, 'source_axis': axis,
                           'packed_visible_bounds': [left + visible[0], top + visible[1],
                                                     left + visible[2], top + visible[3]]})
    atlas.save(ROOT / 'res/boss-atlas.png', optimize=True)
    metadata = {'source': source.name, 'source_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
                'walk_fix_source': fixed_source.name, 'walk_fix_sha256': hashlib.sha256(fixed_source.read_bytes()).hexdigest(),
                'source_size': list(image.size), 'cell_size': list(CELL), 'anchor': list(ANCHOR),
                'directions': ['down', 'left', 'right', 'up'],
                'columns': ['idle', 'walk_contact_right', 'walk_pass_right', 'walk_contact_left',
                            'walk_pass_left', 'windup', 'strike', 'fallen'], 'frames': frames}
    (source.parent / 'frames.json').write_text(json.dumps(metadata, indent=2) + '\n', encoding='utf-8')
    print('Packed 32 boss frames:', atlas.size)

if __name__ == '__main__':
    main()
