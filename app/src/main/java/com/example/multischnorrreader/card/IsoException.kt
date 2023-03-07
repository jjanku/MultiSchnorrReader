package com.example.multischnorrreader.card

class IsoException(val status: Int) :
    Exception("Card returned status %#06x".format(status))