package Crawler

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

class DatabaseAccess(val sqlPrefix: String, private val connection: Connection) {

    init {
        println("Setting up database...")
        val statement = connection.createStatement()
        try {
            statement.executeUpdate(sqlFromResource("/$sqlPrefix/db_setup.sql"))
        } catch (e: SQLException) {
            println("Error setting up database.")
            throw RuntimeException(e)
        } catch (e: IOException) {
            println("Couldn't read setup file.")
            throw RuntimeException(e)
        }

        Runtime.getRuntime().addShutdownHook(Thread(::terminate))
        println("Database setup complete.")
    }


    private fun link(lid: Int, rid: Int, pst: PreparedStatement) {
        pst.setInt(1, lid)
        pst.setInt(2, rid)
        try {
            pst.executeUpdate()
        } catch (e: SQLException) {
            println("Error with link.'")
            println("lid = ${Math.min(lid, rid)} rid = ${Math.max(lid, rid)}")
            throw e
        }
    }

    private fun setupLogic(data: String, readPst: PreparedStatement, writePst: PreparedStatement, idColumn: String): Int {
        readPst.setString(1, data)
        var result = try {
            readPst.executeQuery()
        } catch (e: SQLException) {
            println("Error with '$data'.")
            throw RuntimeException(e)
        }
        return if (result.next())
            result.getInt(idColumn)
        else {
            writePst.setString(1, data)
            try {
                writePst.executeUpdate()
            } catch (e: SQLException) {
                println("Error with '$data'.")
                throw e
            }
            writePst.generatedKeys.next()
            writePst.generatedKeys.getInt(1)
        }
    }

    private fun prepare(resource: String) = connection.prepareStatement(sqlFromResource(resource))

    private fun prepareIdReturn(resource: String) = connection.prepareStatement(sqlFromResource(resource), PreparedStatement.RETURN_GENERATED_KEYS)

    private fun sqlFromResource(resource: String): String {
        val reader = BufferedReader(InputStreamReader(this.javaClass.getResourceAsStream(resource)))
        return reader.lineSequence().joinToString(separator = " "){ it }
    }

    fun disconnect() {
        if (!connection.autoCommit)
            connection.commit()
        connection.close()
    }


    private fun terminate() {
        if(!connection.isClosed) {
            if (!connection.autoCommit)
                connection.rollback()
            connection.close()
        }
    }

}