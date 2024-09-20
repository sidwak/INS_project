from sympy import factorint

def extended_gcd(a, b):
    """ Extended Euclidean Algorithm to find the greatest common divisor of a and b, and the coefficients of Bézout's identity. """
    if a == 0:
        return (b, 0, 1)
    gcd, x1, y1 = extended_gcd(b % a, a)
    x = y1 - (b // a) * x1
    y = x1
    return (gcd, x, y)

def modular_inverse(e, phi):
    """ Compute the modular inverse of e under modulo phi. """
    gcd, x, y = extended_gcd(e, phi)
    if gcd != 1:
        raise ValueError("No modular inverse exists for the given e and phi.")
    return x % phi

def factorization_attack(n, e, ciphertext):
    # Factorize the modulus n into its prime components
    factors = factorint(n)
    
    if len(factors) != 2:
        print("Cannot factorize. Make sure n is a product of exactly two primes.")
        return

    p, q = factors.keys()
    phi = (p - 1) * (q - 1)

    # Compute the private exponent d
    d = modular_inverse(e, phi)

    # Decrypt the ciphertext using the private key
    decrypted_text = pow(ciphertext, d, n)

    # Print the results
    print(f"Found p: {p}")
    print(f"Found q: {q}")
    print(f"Decrypted text: {decrypted_text}")

    
n = int(input('Enter the value of n = '))  # RSA modulus
e = int(input('Enter the value of e = '))  # Public exponent
ciphertext = int(input('Enter the value of ciphertext = '))  # Encrypted message

# Perform the factorization attack
factorization_attack(n, e, ciphertext)
