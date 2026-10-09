#!/usr/bin/env python3
"""Assemble the real MIDP frames from BelfryAnimationPreview into looping GIFs."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]


def pack(prefix, name):
    files = sorted((ROOT / 'preview').glob(prefix + '[0-9][0-9][0-9].png'))
    if not files:
        raise SystemExit('Run BelfryAnimationPreview first: missing ' + prefix)
    frames = [Image.open(path).convert('RGB') for path in files]
    if any(frame.size != (320, 240) for frame in frames):
        raise ValueError('Expected unmodified 320x240 MIDP frames')
    # One palette for the entire clip prevents colour changes between frames.
    strip = Image.new('RGB', (320, 240 * len(frames)))
    for index, frame in enumerate(frames):
        strip.paste(frame, (0, index * 240))
    palette = strip.quantize(colors=256)
    output = [frame.resize((640, 480), Image.Resampling.NEAREST).quantize(
        palette=palette, dither=Image.Dither.NONE) for frame in frames]
    target = ROOT / 'preview' / name
    output[0].save(target, save_all=True, append_images=output[1:],
                   duration=120, loop=0, disposal=2, optimize=False)
    print('Saved', target, 'from', len(frames), 'real MIDP frames')


if __name__ == '__main__':
    pack('belfry-animation-', 'belfry-animations.gif')
    pack('bonfire-rest-animation-', 'bonfire-rest.gif')
