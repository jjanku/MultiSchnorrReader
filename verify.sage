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
        "x": 97451645805052459860544257111023732257189445793566253538963642236580295025715,
        "y": 108036769450804252195526975423364810282508296824311571541272318567742502569515
    },
    "message": "68656c6c6f000000000000000000000000000000000000000000000000000000",
    "nonce": {
        "x": 49759968054553596793774934369901300558457422800809412179313891716327499250464,
        "y": 10595451351086868087048689286043643391031486060528063929912762467167368993502
    },
    "signature": 15093668512161333926586696895604108962007543668405816918671093546412761219977
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
c = int.from_bytes(sha256(encode_point(R) + encode_point(X) + m).digest())

assert s * G == R + c * X
