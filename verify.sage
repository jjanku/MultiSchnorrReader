import json
from hashlib import sha256


# https://neuromancer.sk/std/secg/secp256k1
p = 0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f
K = GF(p)
a = K(0x0000000000000000000000000000000000000000000000000000000000000000)
b = K(0x0000000000000000000000000000000000000000000000000000000000000007)
E = EllipticCurve(K, (a, b))
G = E(
    0x79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798,
    0x483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8
)
E.set_order(0xfffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141 * 0x1)

COORD_LEN = (int(p).bit_length() + 7) // 8

# TODO: insert your signature here
data_json = '''
{
    "group": {
        "x": 82621371836311322635731610237257798577123535778674168916413120265184559472745,
        "y": 21073772193838990103938873926270519023402430518253746786624584882368307768791
    },
    "message": "68656c6c6f000000000000000000000000000000000000000000000000000000",
    "nonce": {
        "x": 28362538501509449895377600989128143307925603662865794767456834195405898871613,
        "y": 16208392399936672946292064678176285964271568821992841796429050116123184365933
    },
    "signature": 104434989019765740973662633870938994847065819938220884369704931829053739864387
}
'''


def encode_point(P):
    x, y = map(lambda z: int(z).to_bytes(COORD_LEN), P.xy())
    return b'\x04' + x + y


data = json.loads(data_json)
X = E(data['group']['x'], data['group']['y'])
R = E(data['nonce']['x'], data['nonce']['y'])
m = bytes.fromhex(data['message'])
s = data['signature']
c = int.from_bytes(sha256(encode_point(X) + m + encode_point(R)).digest())

assert s * G == R + c * X
