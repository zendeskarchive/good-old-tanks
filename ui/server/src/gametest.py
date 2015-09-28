import unittest
from game import *


class GameTest(unittest.TestCase):

    def setUp(self):
        self.game = DummyGame.create()

    def test_next_round(self):
        self.assertEquals(self.game.rounds[1].next, 2, 'incorrect next attribute')
        self.assertEquals(self.game.rounds[2].next, 3, 'incorrect next attribute')
        self.assertEquals(self.game.rounds[3].next, 4, 'incorrect next attribute')
        self.assertEquals(self.game.rounds[4].next, None, 'incorrect next attribute')

    def test_representation(self):
        self.assertEquals(self.game.rounds[1].representation(),
                          {'id': 1, 'next': 2, 'tanks': [
                              {'angle': -90, 'exists': True, 'power': 300, 'fire': False, 'player': 'Jools',
                               'health': 100, 'y': 340, 'x': 100},
                              {'angle': -45, 'exists': True, 'power': 300, 'fire': False, 'player': 'Joops',
                               'health': 100, 'y': 150, 'x': 430}]} )


if __name__ == '__main__':
    unittest.main()
