
class Tank(object):

    def __init__(self, x=0, y=0, angle=0, power=300, player='', health=100, fire=False, exists=True):
        self.x = x
        self.y = y
        self.angle = angle
        self.power = power
        self.player = player
        self.health = health
        self.fire = fire
        self.exists = exists

    def withAttrs(self, attrs):
        copied = self._copy()
        copied.fire = False
        [copied.__setattr__(name, value) for name, value in attrs.iteritems()]
        return copied

    def _copy(self):
        return Tank(self.x, self.y, self.angle, self.power, self.player, self.health, self.fire, self.exists)

    def representation(self):
        return self.__dict__


class Round(object):

    def __init__(self, id, tanks=[]):
        self.id = id
        self.tanks = tanks[:]
        self.next = None

    def representation(self):
        return { 'id': self.id, 'tanks': [ t.representation() for t in self.tanks ], 'next': self.next }


class Game(object):

    def __init__(self, rounds=[]):
        self.rounds = {}
        if not rounds:
            return
        firstRound = rounds[0]
        self.rounds[firstRound.id] = firstRound
        id = firstRound.id
        for round in rounds[1:]:
            self.rounds[id].__setattr__('next', round.id)
            id = round.id
            self.rounds[id] = round

    def representation(self):
        return {'game': [ r.representation() for r in self.rounds.values() ] }


class DummyGame(object):

    @classmethod
    def create(cls):
        t1 = Tank(100, 340, -90, player='Jools')
        t2 = Tank(430, 150, -45, player='Joops')
        r1 = Round(1, [t1, t2])

        t1 = t1.withAttrs({ 'x': 110, 'y': 320, 'angle': -45 })
        t2 = t2.withAttrs({ 'angle': -110, 'fire': True })
        r2 = Round(2, [t1, t2])

        t1 = t1.withAttrs({ 'power': 50, 'fire': True })
        t2 = t2.withAttrs({ 'x': 450, 'y': 140, 'angle': -140 })
        r3 = Round(3, [t1, t2])

        t1 = t1.withAttrs({ 'x': 120 })
        t2 = t2.withAttrs({ 'exists': False })
        r4 = Round(4, [t1, t2])

        return Game([r1, r2, r3, r4])
