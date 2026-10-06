#!/usr/bin/env python3
"""Pack action sheets with per-frame anchors and binary MIDP transparency."""
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageOps


def alpha_from_black_matte(image):
    """Remove exterior black matte while preserving enclosed black armor details."""
    image = image.convert('RGBA')
    mask = Image.new('L', image.size)
    pixels = image.load()
    rgba = (pixels[x, y] for y in range(image.height) for x in range(image.width))
    # Low neutral tones are backdrop; retain colored outlines and white blade highlights.
    mask.putdata([
        0 if max(r, g, b) <= 18 or (max(r, g, b) < 64 and max(r, g, b) - min(r, g, b) <= 8) else 255
        for r, g, b, a in rgba
    ])
    if mask.getpixel((0, 0)) != 0:
        raise ValueError('Black-matte source must have a clear top-left margin')
    ImageDraw.floodfill(mask, (0, 0), 128)
    image.putalpha(mask.point(lambda value: 0 if value == 128 else 255))
    return image


def pack_atlas(source, metadata):
    actions = ['idle', 'roll', 'heal', 'attack', 'walk', 'death']
    cell_width = metadata.get('atlas_frame_width', 64)
    cell_height = metadata.get('atlas_frame_height', 64)
    default_scale = .26
    atlas = Image.new('RGBA', (cell_width * 6, cell_height * 24), (0, 0, 0, 0))
    for group, action in enumerate(actions):
        sheet = metadata['sheets'][action]
        path = source / sheet['filename']
        if sheet.get('source_sha256'):
            if hashlib.sha256(path.read_bytes()).hexdigest() != sheet['source_sha256']:
                raise ValueError('Source checksum mismatch: ' + str(path))
        image = Image.open(path).convert('RGBA')
        expected_size = (sheet.get('sheet_width', metadata['sheet_width']),
                         sheet.get('sheet_height', metadata['sheet_height']))
        if image.size != expected_size:
            raise ValueError('Unexpected source dimensions for ' + action)
        background_mode = sheet.get('background_mode', 'alpha')
        if background_mode == 'black_matte':
            image = alpha_from_black_matte(image)
        elif background_mode != 'alpha':
            raise ValueError('Unknown background mode: ' + background_mode)
        scales = sheet.get('direction_scales', [sheet.get('scale', default_scale)] * 4)
        if len(scales) != 4 or any(scale <= 0 for scale in scales):
            raise ValueError('Expected four positive direction scales for ' + action)
        if {(f['row'], f['column']) for f in sheet['frames']} != {(r, c) for r in range(4) for c in range(6)} or len(sheet['frames']) != 24:
            raise ValueError('Expected each of the 24 frame positions once for ' + action)
        for frame in sheet['frames']:
            row, col = frame['row'], frame['column']
            x, y, width, height = (frame[k] for k in ['x', 'y', 'width', 'height'])
            if x < 0 or y < 0 or width <= 0 or height <= 0 or x + width > image.width or y + height > image.height:
                raise ValueError('Frame outside source image: ' + action + '/' + str((row, col)))
            tile = image.crop((x, y, x + width, y + height))
            anchor_x = frame['anchor_x']
            # Legacy idle/heal/death profiles face opposite the configured direction.
            mirror_profiles = sheet.get('mirror_profiles', action in ['idle', 'heal', 'death'])
            if mirror_profiles and row in [1, 2]:
                tile = ImageOps.mirror(tile)
                anchor_x = width - 1 - anchor_x
            scale = scales[row]
            tile = tile.resize((round(width * scale), round(height * scale)), Image.Resampling.NEAREST)
            tile.putalpha(tile.getchannel('A').point(lambda alpha: 255 if alpha >= 128 else 0))
            left, top = cell_width // 2 - round(anchor_x * scale), 52 - round(frame['anchor_y'] * scale)
            if sheet.get('ground_anchor_radius'):
                # Anchor after resampling so fractional scales cannot move planted soles by a pixel.
                center = round(anchor_x * tile.width / width)
                radius = round(sheet['ground_anchor_radius'] * tile.width / width)
                ground = tile.getchannel('A').crop((max(0, center - radius), 0,
                                                   min(tile.width, center + radius + 1), tile.height)).getbbox()
                if ground is None:
                    raise ValueError('Missing ground anchor: ' + action + '/' + str((row, col)))
                top = 52 - (ground[3] - 1)
            visible = tile.getchannel('A').getbbox()
            if visible is None or left + visible[0] < 0 or top + visible[1] < 0 or left + visible[2] > cell_width or top + visible[3] > cell_height:
                raise ValueError('Frame does not fit atlas cell: ' + action + '/' + str((row, col)))
            atlas.paste(tile, (col * cell_width + left, (group * 4 + row) * cell_height + top), tile)
    return atlas


def main():
    root = Path(__file__).resolve().parents[1]
    source = root / 'assets/knight-sheets'
    metadata = json.loads((source / 'frames.json').read_text())
    atlas = pack_atlas(source, metadata)
    atlas.save(root / 'res/hero-atlas.png', optimize=True)
    print('Packed 144 frames:', atlas.size)


if __name__ == '__main__':
    main()
