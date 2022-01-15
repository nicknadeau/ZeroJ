# ZeroJ
An implementation of the Zero blockchain layer specification in Java.

See the [Zero-Specification](https://github.com/nicknadeau/Zero-Specification) repository for the specification itself.

__WARNING__: ZeroJ is still version 0 because there is no substantial layer one protocol built on top of it yet, and I refuse to solidify the implementation to v1 until I have done some dog-fooding of my own and have a real and comprehensive use-case to test it out. I am currently working on that implementation, but I don't expect it to materialize any time soon. Until then, this code should still be considered experimental and is for use at your own risk (it's always for use at your own risk, I take no responsibilities! - _but_, I do not guarantee that interfaces will not be broken between now and the eventual v1 release).

## Requirements
* JDK 11
* Gradle 6.3

## Building And Testing
```shell
# To build:
./gradlew build
# To test:
./gradlew test
```

## Usage
ZeroJ is intended to be embedded into a layer one blockchain implementation. It is highly recommended that the short Zero specification be read so that nomenclature, the role of ZeroJ, and certain interfaces that must be implemented are all clear.

The primary class of concern is `ZeroBlockchain`. To build a new instance of `ZeroBlockchain`, its internal builder class can be used:
```java
ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
	.withDatabase(/*a database implementation*/)
	.withHashFunction(/*a hash function implementation*/)
	.withSignatureVerifier(/*a signature verifier implementation*/)
	.withCallbacks(/*a callbacks implementation*/)
	.build();
```

To work with ZeroJ, the layer one protocol must implement all of the following interfaces:
* `Block`
* `LayerOneValidateBlockCallback`
* `LayerOneAddBlockCallback`
* `LayerOneDeleteBlockCallback`
* `ZeroDatabase`
* `HashFunction`
* `SignatureVerifier`
