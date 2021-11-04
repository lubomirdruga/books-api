# Memory allocation after running all unit tests BEFORE profilation fixes

- ~1.350 GB

- the most memory takes the `deleteAuthor_shouldDeleteOnlyBookExclusiveForDeletedAuthor()` and its calls
  to `NativeQueryRepository` which were tested - ~85-90 MB

# Memory allocation after running all unit tests AFTER profilation fixes

- ~1.350 GB

- using DB cascade operation & rewritten other Native
  queries `deleteAuthor_shouldDeleteOnlyBookExclusiveForDeletedAuthor` operations takes ~84-88 MB

