import os
import sys
import binascii

from Crypto.Protocol.KDF import PBKDF2
from Crypto.Hash import SHA256, HMAC
from Crypto.Cipher import AES

tohex = binascii.b2a_hex
frhex = binascii.a2b_hex
tob64 = binascii.b2a_base64

hmac_sha256 = lambda p, s: HMAC.new(p, s, SHA256).digest()

salt = ''.join('\x01' for i in range(16))
key = PBKDF2('abcd', salt, 16*4, 128 * 1000, hmac_sha256)
enckey = key[0:16]
hmackey = key[16:32]

print 'keys'
print tohex(enckey)
print tohex(hmackey)
print tohex(key[16*2 : 16*3])
print tohex(key[16*3 : 16*4])
print

plaintext = "AABBCCDDEEFFGGHHIIJJKKLLMMNNOOPPQQRRSSTTUUVVWWXXYYZZ"
iv = "aaaaaaaaaaaaaaaa"
padlen = 16 - (len(plaintext) % 16)
pad = ''.join([chr(padlen) for _ in range(padlen)])

aes = AES.new(enckey, AES.MODE_CBC, iv)
hmac = HMAC.new(hmackey, digestmod=SHA256)

cr = aes.encrypt(plaintext + pad)
hmac.update(cr)
auth = hmac.digest()

print 'iv', tob64(iv)
print 'salt', tob64(salt)
print 'hmac', tob64(auth)
print 'cryptext', tob64(cr)
