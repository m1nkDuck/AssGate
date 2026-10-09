import hashlib
import json
import unittest
from pathlib import Path

from PIL import Image
from tools.pack_rest_sprites import pack_rest


class RestPackingTest(unittest.TestCase):
    def test_source_and_standing_reference_rebuild_the_shipped_rest_atlas(self):
        root = Path(__file__).resolve().parents[1]
        source = root / 'assets/knight-sheets/knight_rest.png'
        idle = root / 'res/hero-atlas.png'
        metadata = json.loads((source.parent / 'rest-frames.json').read_text())
        self.assertEqual(hashlib.sha256(source.read_bytes()).hexdigest(), metadata['source_sha256'])
        self.assertEqual(hashlib.sha256(idle.read_bytes()).hexdigest(), metadata['standing_reference_sha256'])
        rebuilt, frames = pack_rest(source, Image.open(idle).convert('RGBA'))
        self.assertEqual(rebuilt.tobytes(), Image.open(root / 'res/hero-rest-atlas.png').convert('RGBA').tobytes())
        self.assertEqual(frames, metadata['frames'])


if __name__ == '__main__':
    unittest.main()
