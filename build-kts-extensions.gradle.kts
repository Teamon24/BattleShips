fun createFile(path: String) = File(path).apply { createNewFile() }
fun propNameEqual(line: String, another: String) = extractPropName(line) == extractPropName(another)
fun extractPropName(line: String) = line.substring(0, line.indexOf("="))
fun playerPropName(i: Int) = when (i) {
    0 -> extra["appPropNameDev"]
    else -> "${extra["playerPropPrefix"]}-${i}"
}

fun propFilepath(name: String) = "${extra["playerPropPrefix"]}${name}.properties"
fun propFile(path: String) = File(propFilepath((extra[path] as String)))

extra["createFile"]      = { it: String -> createFile(it) }
extra["propNameEqual"]   = { line: String, another: String -> propNameEqual(line, another) }
extra["playerPropName"]  = { it: Int -> playerPropName(it) }
extra["propFilepath"]    = { it: String -> propFilepath(it) }
extra["propFile"]        = { it: String -> propFile(it) }
