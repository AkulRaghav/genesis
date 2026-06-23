package dev.genesis.templates

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class HtmlDslTest : FunSpec({

    test("builds basic HTML structure") {
        val result = html {
            doctype()
            html("en") {
                head {
                    title("Test Page")
                    metaCharset()
                }
                body {
                    h1 { text("Hello World") }
                    p { text("A paragraph") }
                }
            }
        }

        result shouldContain "<!DOCTYPE html>"
        result shouldContain """<html lang="en">"""
        result shouldContain "<title>Test Page</title>"
        result shouldContain "<h1>Hello World</h1>"
        result shouldContain "<p>A paragraph</p>"
    }

    test("builds elements with attributes") {
        val result = html {
            html {
                body {
                    div {
                        id("main")
                        className("container")
                        text("Content")
                    }
                }
            }
        }

        result shouldContain """id="main""""
        result shouldContain """class="container""""
    }

    test("builds links") {
        val result = html {
            html {
                body {
                    a("/about") { text("About") }
                }
            }
        }

        result shouldContain """<a href="/about">About</a>"""
    }

    test("escapes HTML entities in text") {
        val result = html {
            html {
                body {
                    p { text("<script>alert('xss')</script>") }
                }
            }
        }

        result shouldContain "&lt;script&gt;"
        result shouldContain "&lt;/script&gt;"
    }

    test("allows raw HTML injection") {
        val result = html {
            html {
                body {
                    raw("<div class=\"custom\">Raw HTML</div>")
                }
            }
        }

        result shouldContain """<div class="custom">Raw HTML</div>"""
    }

    test("builds meta tags") {
        val result = html {
            html {
                head {
                    metaViewport()
                    meta("description", "A test site")
                }
            }
        }

        result shouldContain """content="width=device-width, initial-scale=1.0""""
        result shouldContain """name="description" content="A test site""""
    }

    test("builds nested structures") {
        val result = html {
            html {
                body {
                    nav {
                        ul {
                            li { a("/") { text("Home") } }
                            li { a("/about") { text("About") } }
                        }
                    }
                }
            }
        }

        result shouldContain "<nav>"
        result shouldContain "<ul>"
        result shouldContain "<li>"
        result shouldContain """<a href="/">Home</a>"""
    }
})
