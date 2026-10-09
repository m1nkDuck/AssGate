#!/usr/bin/env python3
"""Pack armed Belfry guards from isolated poses without cutting long weapons."""
import hashlib
import json
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageOps

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
from tools.pack_boss_sprites import components

CELL = (128, 80)
ANCHOR = (64, 68)


def separate_touching_rows(image):
    """Find the transparent gutters around the tightly spaced walk poses.

    A straight grid cut would shave soles or helmet plumes. Follow the
    lowest-alpha seam instead, retaining each source pixel on one side.
    """
    alpha = image.getchannel('A')
    pixels = alpha.load()
    seams = []
    for low, high, center in [(238, 260, 248), (445, 470, 458), (656, 683, 670)]:
        ys = list(range(low, high + 1))
        costs = [0] * len(ys)
        previous = []
        for x in range(image.width):
            next_costs, parents = [], []
            for i, y in enumerate(ys):
                start, end = max(0, i - 1), min(len(ys), i + 2)
                parent = min(range(start, end), key=lambda j: costs[j] + abs(i - j))
                # Expensive only when the boundary cuts through an opaque
                # connection. The small center bias keeps empty gutters flat.
                connection = min(pixels[x, y - 1], pixels[x, y])
                next_costs.append(costs[parent] + abs(i - parent) + connection * 100 + abs(y - center))
                parents.append(parent)
            costs = next_costs
            previous.append(parents)
        index = min(range(len(ys)), key=lambda i: costs[i])
        seam = [0] * image.width
        for x in range(image.width - 1, -1, -1):
            seam[x] = ys[index]
            index = previous[x][index]
        seams.append(seam)
    rows = []
    for row in range(4):
        layer = Image.new('RGBA', image.size)
        source, target = image.load(), layer.load()
        for x in range(image.width):
            start = seams[row - 1][x] if row else 0
            end = seams[row][x] if row < 3 else image.height
            for y in range(start, end):
                target[x, y] = source[x, y]
        rows.append(components(layer))
    return rows


def read_rows(path, columns):
    image = Image.open(path).convert('RGBA')
    if path.name == 'spearman_walk_final.png':
        rows = separate_touching_rows(image)
    else:
        rows = [[] for _ in range(4)]
        for pose in components(image):
            row = min(3, max(0, round((pose['box'][3] - (image.height / 4 - 10)) / (image.height / 4))))
            rows[row].append(pose)
    if any(len(row) != columns for row in rows):
        raise ValueError(str(path.name) + ': expected ' + str(columns) + ' complete figures per row, got ' + str([len(row) for row in rows]))
    for row in rows:
        row.sort(key=lambda pose: pose['box'][0])
    return image, rows


def pack(kind):
    folder = ROOT / 'assets/belfry-sheets'
    if kind == 'guard':
        sources = {name: read_rows(folder / name, 8) for name in ['guard.png', 'guard_swords_fixed.png']}
        layout = [('guard.png', col) for col in range(8)]
        height = 44
    else:
        sources = {name: read_rows(folder / name, 4) for name in ['spearman_walk_final.png', 'spearman_actions.png']}
        layout = [('spearman_actions.png', 0)] + [('spearman_walk_final.png', col) for col in range(4)] + [('spearman_actions.png', col) for col in [1, 2, 3]]
        height = 48
    atlas = Image.new('RGBA', (CELL[0] * 8, CELL[1] * 4))
    frames = []
    for row in range(4):
        idle_image, idle_rows = sources[layout[0][0]]
        idle = idle_rows[row][layout[0][1]]['box']
        scale = height / (idle[3] - idle[1])
        for col, (name, source_col) in enumerate(layout):
            if kind == 'guard' and row in (1, 2) and col == 2:
                name = 'guard_swords_fixed.png'
            image, rows = sources[name]
            frame_scale = scale
            if kind == 'spearman' and name == 'spearman_walk_final.png':
                # Separate generation has a different source pixel density. One
                # scale per direction preserves the walk's changing leg heights.
                heights = [p['box'][3] - p['box'][1] for p in rows[row]]
                frame_scale = height / (sum(heights) / len(heights))
            # The generated sword cut turned right in the left-facing row.
            # Reflect the complete opposite pose, including its connected weapon.
            mirrored = kind == 'guard' and row == 1 and col in (5, 6)
            source_row = 2 if mirrored else row
            pose = rows[source_row][source_col]
            x0, y0, x1, y1 = pose['box']
            tile = Image.new('RGBA', (x1 - x0, y1 - y0))
            pixels = image.load()
            target = tile.load()
            for index in pose['points']:
                x, y = index % image.width, index // image.width
                target[x - x0, y - y0] = pixels[x, y][:3] + (255,)
            # Isolate the body axis from the weapon's far-reaching tip.
            head = [index % image.width for index in pose['points'] if index // image.width < y0 + 30]
            axis = round(sum(head) / len(head)) - x0 if col != 7 else (x1 - x0) // 2
            if mirrored:
                tile = ImageOps.mirror(tile)
                axis = tile.width - 1 - axis
            tile = tile.resize((round(tile.width * frame_scale), round(tile.height * frame_scale)), Image.Resampling.NEAREST)
            bounds = tile.getchannel('A').getbbox()
            left = ANCHOR[0] - round(axis * frame_scale)
            top = ANCHOR[1] - (bounds[3] - 1)
            if left + bounds[0] < 1 or left + bounds[2] > CELL[0] - 1 or top + bounds[1] < 1 or top + bounds[3] > CELL[1] - 1:
                raise ValueError('Clipped ' + kind + ' pose: ' + str((row, col)))
            atlas.paste(tile, (col * CELL[0] + left, row * CELL[1] + top), tile)
            frames.append({'row': row, 'column': col, 'source_filename': name,
                           'source_row': source_row, 'source_column': source_col,
                           'source_box': pose['box'], 'mirror': mirrored, 'source_axis': axis,
                           'scale': frame_scale, 'packed_visible_bounds': [left + bounds[0], top + bounds[1], left + bounds[2], top + bounds[3]]})
    atlas.save(ROOT / ('res/belfry-' + kind + '-atlas.png'), optimize=True)
    metadata = {'sources': {name: hashlib.sha256((folder / name).read_bytes()).hexdigest() for name in sources},
                'cell_size': list(CELL), 'anchor': list(ANCHOR), 'standing_height': height,
                'directions': ['down', 'left', 'right', 'up'],
                'columns': ['idle', 'walk_left_contact', 'walk_left_pass', 'walk_right_contact', 'walk_right_pass', 'windup', 'strike', 'fallen'],
                'frames': frames}
    (folder / (kind + '-frames.json')).write_text(json.dumps(metadata, indent=2) + '\n', encoding='utf-8')
    contact = Image.new('RGB', atlas.size, (22, 28, 39));contact.paste(atlas, (0, 0), atlas)
    draw = ImageDraw.Draw(contact)
    for x in range(CELL[0], atlas.width, CELL[0]):draw.line((x, 0, x, atlas.height), fill=(52, 57, 69))
    for y in range(CELL[1], atlas.height, CELL[1]):draw.line((0, y, atlas.width, y), fill=(52, 57, 69))
    contact.resize((atlas.width * 2, atlas.height * 2), Image.Resampling.NEAREST).save(ROOT / ('preview/belfry-' + kind + '-packing.png'))
    print('Packed 32 ' + kind + ' poses:', atlas.size)


if __name__ == '__main__':
    for kind in sys.argv[1:] or ['guard', 'spearman']:
        pack(kind)
