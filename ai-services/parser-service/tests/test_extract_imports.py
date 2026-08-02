import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pytest
from server import extract_imports

PYTHON_SOURCE = b"import os\nfrom typing import List\n"
JS_SOURCE = b"import React from 'react';\nimport { useState } from 'react';\n"
JAVA_SOURCE = b"package com.example;\nimport java.util.List;\nimport com.foo.Bar;\n"
GO_SOURCE = b'package main\nimport (\n\t"fmt"\n\t"os"\n)\n'
RUST_SOURCE = b"use std::collections::HashMap;\nuse foo::bar::Baz;\n"


@pytest.mark.parametrize("source,language,expected_substrings", [
    (PYTHON_SOURCE, "python", ["os", "typing"]),
    (JS_SOURCE, "javascript", ["react"]),
    (JAVA_SOURCE, "java", ["java.util.List", "com.foo.Bar"]),
    (GO_SOURCE, "go", ["fmt", "os"]),
    (RUST_SOURCE, "rust", ["std::collections::HashMap", "foo::bar::Baz"]),
])
def test_extract_imports_returns_expected(source, language, expected_substrings):
    result = extract_imports(source, language)
    joined = " ".join(result)
    for expected in expected_substrings:
        assert expected in joined, f"expected '{expected}' in {result} for language={language}"


def test_extract_imports_go_strips_quotes():
    result = extract_imports(GO_SOURCE, "go")
    assert all(not r.startswith('"') for r in result), "Go import paths should have quotes stripped"


def test_extract_imports_unsupported_language_returns_empty_list():
    assert extract_imports(b"whatever", "cobol") == []


def test_extract_imports_malformed_source_does_not_raise():
    garbage = b"import until this is {{{{ not valid syntax at all"
    result = extract_imports(garbage, "python")
    assert isinstance(result, list)