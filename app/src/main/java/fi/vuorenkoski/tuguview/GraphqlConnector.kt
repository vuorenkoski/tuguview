package fi.vuorenkoski.tuguview

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date

class GraphQLConnector {
    private var graphqlEndpoint: String? = null
    private var token: String? = null

    @Throws(Exception::class)
    fun login(username: String?, password: String?, endpoint: String) {
        val loginQuery = String.format(
            "mutation { login(username: \"%s\", password: \"%s\") { token } }",
            username,
            password
        )
        val loginRequest = "{\"query\":\"" + loginQuery.replace("\"", "\\\"") + "\"}"

        // Send login mutation
        val url = URL(endpoint)
        val loginConn = url.openConnection() as HttpURLConnection
        loginConn.setRequestMethod("POST")
        loginConn.setRequestProperty("Content-Type", "application/json")
        loginConn.setDoOutput(true)
        loginConn.getOutputStream().use { os ->
            val input = loginRequest.toByteArray(charset("utf-8"))
            os.write(input, 0, input.size)
        }
        val loginResponse = StringBuilder()
        val loginCode = loginConn.getResponseCode()
        val loginBr: BufferedReader?
        if (loginCode >= 200 && loginCode < 300) {
            loginBr = BufferedReader(InputStreamReader(loginConn.getInputStream(), "utf-8"))
        } else {
            loginBr = BufferedReader(InputStreamReader(loginConn.getErrorStream(), "utf-8"))
        }
        var loginLine: String?
        while ((loginBr.readLine().also { loginLine = it }) != null) {
            loginResponse.append(loginLine!!.trim { it <= ' ' })
        }
        loginBr.close()

        // Parse token from login response
        val loginJson = JSONObject(loginResponse.toString())
        token = null
        try {
            token = loginJson.getJSONObject("data").getJSONObject("login").getString("token")
            graphqlEndpoint = endpoint
        } catch (e: Exception) {
            throw RuntimeException(
                "" + loginJson.getJSONArray("errors").getJSONObject(0).getString("message"),
                e
            )
        }
    }

    // Fetches switches from the GraphQL server
    @Throws(Exception::class)
    fun fetchSwitches(): MutableList<Switch> {
        val query = "{ allSwitches { id description on updatedAt } }"
        val jsonRequest = "{\"query\":\"" + query.replace("\"", "\\\"") + "\"}"

        val url = URL(graphqlEndpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestMethod("POST")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer " + token)
        conn.setDoOutput(true)
        conn.getOutputStream().use { os ->
            val input = jsonRequest.toByteArray(charset("utf-8"))
            os.write(input, 0, input.size)
        }
        val response = StringBuilder()
        val responseCode = conn.getResponseCode()
        val br: BufferedReader?
        if (responseCode >= 200 && responseCode < 300) {
            br = BufferedReader(InputStreamReader(conn.getInputStream(), "utf-8"))
        } else {
            br = BufferedReader(InputStreamReader(conn.getErrorStream(), "utf-8"))
        }
        var line: String?
        while ((br.readLine().also { line = it }) != null) {
            response.append(line!!.trim { it <= ' ' })
        }
        br.close()

        // Parse JSON response
        val jsonResponse = JSONObject(response.toString())
        val switchesArray = jsonResponse.getJSONObject("data").getJSONArray("allSwitches")
        val switches: MutableList<Switch> = ArrayList<Switch>()
        for (i in 0..<switchesArray.length()) {
            val switchObj = switchesArray.getJSONObject(i)
            val id = switchObj.optString("id", "")
            val name = switchObj.optString("description", "")
            val updatedAt = switchObj.optString("updatedAt", "")
            val date = Date(updatedAt.toLong() * 1)
            val on = switchObj.optString("on", "")
            var onb = "ON"
            if (on == "false") {
                onb = "OFF"
            }
            switches.add(Switch(id, name, onb, date))
        }
        return switches
    }

    // Change switch
    @Throws(Exception::class)
    fun setSwitch(switchId: String, command: Boolean) {
        // Escaping the query string is safer
        val query = "mutation setSwitchCommand(\$command: Boolean!, \$setSwitchId: Int!) { setSwitchCommand(command: \$command, id: \$setSwitchId) { id on } }"
        val variables = "\"setSwitchId\":$switchId, \"command\":$command"
        val jsonRequest = "{\"query\":\"${query.replace("\"", "\\\"")}\",\"variables\":{$variables}}"

        Log.d("GraphQLConnector", "Request: $jsonRequest")
        val url = URL(graphqlEndpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer " + token)
        conn.doOutput = true
        conn.outputStream.use { os ->
            val input = jsonRequest.toByteArray(charset("utf-8"))
            os.write(input, 0, input.size)
        }

        val responseCode = conn.responseCode
        // Read the response to complete the HTTP transaction, even if you don't use it.
        val inputStream = if (responseCode in 200..299) {
            conn.inputStream
        } else {
            conn.errorStream
        }
        val response = inputStream.bufferedReader().use { it.readText() }

        Log.d("GraphQLConnector", "Response Code: $responseCode, Body: $response")

        // If the server returns an error code, throw an exception
        if (responseCode !in 200..299) {
            throw Exception("Failed to set switch. Server responded with code $responseCode: $response")
        }
    }

    @Throws(Exception::class)
    fun fetchSensors(): MutableList<Sensor> {
        val query =
            "{ allSensors { id sensorName sensorFullname lastValue sensorUnit lastTimestamp } }"
        val jsonRequest = "{\"query\":\"" + query.replace("\"", "\\\"") + "\"}"

        val url = URL(graphqlEndpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestMethod("POST")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer " + token)
        conn.setDoOutput(true)
        conn.getOutputStream().use { os ->
            val input = jsonRequest.toByteArray(charset("utf-8"))
            os.write(input, 0, input.size)
        }
        val response = StringBuilder()
        val responseCode = conn.getResponseCode()
        val br: BufferedReader?
        if (responseCode >= 200 && responseCode < 300) {
            br = BufferedReader(InputStreamReader(conn.getInputStream(), "utf-8"))
        } else {
            println("HTTP error code: " + responseCode)
            br = BufferedReader(InputStreamReader(conn.getErrorStream(), "utf-8"))
        }
        var line: String?
        while ((br.readLine().also { line = it }) != null) {
            response.append(line!!.trim { it <= ' ' })
        }
        br.close()

        // Parse JSON response
        val jsonResponse = JSONObject(response.toString())
        val sensorsArray = jsonResponse.getJSONObject("data").getJSONArray("allSensors")
        val sensors: MutableList<Sensor> = ArrayList<Sensor>()

        for (i in 0..<sensorsArray.length()) {
            val sensorObj = sensorsArray.getJSONObject(i)
            val id = sensorObj.optString("id", "")
            val sensorFullname = sensorObj.optString("sensorFullname", "")
            val sensorName = sensorObj.optString("sensorName", "")
            val lastValue = sensorObj.optString("lastValue", "")
            val f = lastValue.toDouble()
            val rounded = Math.round(f * 10) / 10.0
            val sensorUnit = sensorObj.optString("sensorUnit", "")
            val lastTimestamp = sensorObj.optString("lastTimestamp", "")
            val date = Date(lastTimestamp.toLong() * 1000)
            sensors.add(Sensor(id, sensorName, sensorFullname, sensorUnit, rounded, date))
        }
        return sensors
    }
}