import unittest

from PIL import Image

from tools.pack_hero_sprites import alpha_from_black_matte


class BlackMatteTest(unittest.TestCase):
    def test_backdrop_removed_without_punching_holes_in_black_armor(self):
        image = Image.new('RGBA', (9, 9), (0, 0, 0, 255))
        for y in range(2, 7):
            for x in range(2, 7):
                image.putpixel((x, y), (20, 40, 200, 255))
        image.putpixel((4, 4), (0, 0, 0, 255))
        result = alpha_from_black_matte(image)
        self.assertEqual(result.getpixel((0, 0))[3], 0)
        self.assertEqual(result.getpixel((4, 4)), (0, 0, 0, 255))
        self.assertEqual(result.getpixel((2, 2)), (20, 40, 200, 255))

    def test_white_blade_and_dark_colored_outlines_survive_neutral_matte(self):
        image = Image.new('RGBA', (9, 9), (28, 28, 29, 255))
        image.putpixel((3, 4), (2, 1, 35, 255))
        image.putpixel((4, 4), (255, 255, 255, 255))
        result = alpha_from_black_matte(image)
        self.assertEqual(result.getpixel((0, 0))[3], 0)
        self.assertEqual(result.getpixel((3, 4)), (2, 1, 35, 255))
        self.assertEqual(result.getpixel((4, 4)), (255, 255, 255, 255))
        self.assertEqual(set(result.getchannel('A').tobytes()), {0, 255})

    def test_non_matte_margin_rejected(self):
        with self.assertRaisesRegex(ValueError, 'clear top-left margin'):
            alpha_from_black_matte(Image.new('RGB', (4, 4), (0, 0, 255)))


if __name__ == '__main__':
    unittest.main()
