package sdr.driver.cp.gradle.licenses

import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

internal class PomParser {

    var name = ""
    var url = ""
    lateinit var licenses: ArrayList<DependencyData.Pom.License>

    private val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun parse(pomFile: File) {
        with(documentBuilder.parse(pomFile)["project"]) {
            name = get("name").textContent
            url = get("url").textContent

            licenses = ArrayList()
            val licenseNodes = get("licenses").childNodes
            for (i in 0 until licenseNodes.length)
                with(licenseNodes.item(i)) {
                    if (nodeName == "license")
                        licenses.add(
                            DependencyData.Pom.License(
                                name = get("name").textContent,
                                url = get("url").textContent
                            )
                        )
                }
        }
    }
}

private operator fun Document.get(tagName: String): Node {
    val nodeList = getElementsByTagName(tagName)
    if (nodeList.length != 1)
        throw Exception("expected exactly one tag named '$tagName', found ${nodeList.length}")
    return nodeList.item(0)
}

private operator fun Node.get(tagName: String): Node {
    var result: Node? = null
    for (i in 0 until childNodes.length) {
        val child = childNodes.item(i)
        if (child.nodeName == tagName) {
            if (result != null)
                throw Exception("more than one tag with name '$tagName' found")
            result = child
        }
    }
    return result ?: throw Exception("tag named '$tagName' not found")
}
