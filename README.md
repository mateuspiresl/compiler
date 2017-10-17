# compiler

Implementation of lexical, syntactic and semantic analyzers.

## Compile

```bash
$ javac -d bin/ src/*.java src/lexical/*.java src/syntactic/*.java src/semantic/*.java src/utils/*.java
```

Or...

```bash
$ sh compile.sh
```

## Run

```
$ java -cp bin/ Program <source_file>
```

## Test

### Success

Each one of the tests above should print "Success!" in the end.

```
$ java -cp bin/ Program test-files/test1.pas
$ java -cp bin/ Program test-files/test2.pas
$ java -cp bin/ Program test-files/test4.pas
```

### Error

Each one of the tests above should print an error message in the end.

```bash
$ java -cp bin/ Program test-files/test3.pas
$ java -cp bin/ Program test-files/semantic/error_expression.pas
$ java -cp bin/ Program test-files/semantic/error_scope.pas
$ java -cp bin/ Program test-files/semantic/error_scope_2.pas
$ java -cp bin/ Program test-files/syntactic/error_closing-comment.pas
$ java -cp bin/ Program test-files/syntactic/error_missing-program.pas
```