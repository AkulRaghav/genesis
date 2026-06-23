package dev.genesis.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import dev.genesis.cli.commands.*

/**
 * Genesis — A modern static site generator for the JVM.
 */
class GenesisCommand : CliktCommand(name = "genesis") {
    override fun help(context: Context) = "Genesis — A modern static site generator for the JVM."
    override fun run() = Unit
}

fun main(args: Array<String>) {
    GenesisCommand()
        .subcommands(
            NewCommand(),
            BuildCommand(),
            ServeCommand(),
            CheckCommand()
        )
        .main(args)
}
