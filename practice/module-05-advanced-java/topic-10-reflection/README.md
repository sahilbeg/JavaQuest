# Reflection

Build a debug printer that works on any Spoke object. `Bike` and `Station` are sample classes to test it on. Leave them as they are.

1. **TODO 1 to 4:** make `describe(o)` return text like `Bike{id=B-1042, battery=80}`. Read every non-static field with reflection, in the order the class declares them.
2. **TODO 5:** make `methodNames(type)` return the names of the public methods the class declares, sorted A to Z.
3. **TODO 6 and 7:** make `create(className)` build an object from its full class name with the no-arg constructor. Throw an `IllegalArgumentException` with a clear message when that fails.
4. **TODO 8:** make `callNoArg(target, methodName)` call a public method with no parameters and return its result.

`getDeclaredFields()` gives fields in declaration order on the standard JVM. The Java rules do not promise it, so the sample classes stay simple.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
