/*
 * Copyright 2017 Cloudopt.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package net.cloudopt.next.yaml

import com.esotericsoftware.yamlbeans.YamlConfig
import com.esotericsoftware.yamlbeans.YamlException
import com.esotericsoftware.yamlbeans.YamlReader
import net.cloudopt.next.aop.Maper
import net.cloudopt.next.yaml.annotation.ConfigureBean
import java.io.File

import java.io.FileNotFoundException
import java.io.FileReader
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/*
 * @author: Cloudopt
 * @Time: 2018/1/4
 * @Description: Used to read yaml
 */
object Yamler {

    private var yamlConfig: YamlConfig? = null

    private var rootPath: String = "";

    init {
        yamlConfig = YamlConfig()
        yamlConfig!!.setAllowDuplicates(false)
        getRootClassPath()
    }

    /**
     * Read YAML file to Map
     * @param filePath File path
     * @param prefix Attribute prefix in yaml
     * @return map
     */
    fun read(filePath: String, prefix: String): Map<*, *>? {
        var map = read(filePath)
        var list = prefix.split(".")
        try {
            for (s in list) {
                map = map!!.get(s) as Map<*, *>
            }
            return map
        } catch (e: RuntimeException) {
            throw RuntimeException("Cloudopt Next Yaml: prefix was not found!")
        }
        return null
    }

    /**
     * Read YAML file to Map
     * @param filePath File path
     * @return map
     */
    fun read(filePath: String): Map<*, *>? {
        var reader: YamlReader? = null
        try {
            reader = YamlReader(FileReader(getRootClassPath() + "/" + filePath), yamlConfig!!)
            var `object`: Any? = null
            `object` = reader.read()
            return `object` as Map<*, *>?
        } catch (e: FileNotFoundException) {
            throw RuntimeException("Cloudopt Next Yaml: $filePath was not found!")
        } catch (e: YamlException) {
            throw RuntimeException("Cloudopt Next Yaml: $filePath cannot be converted to yaml!")
        }

        return null
    }

    /**
     * Read YAML file into a specific instance of the object
     * @param filePath File path
     * @param clazz Class name
     * @return Object
     */
    fun <T> read(filePath: String, clazz: Class<T>): T? {
        var reader: YamlReader? = null
        try {
            reader = YamlReader(FileReader(getRootClassPath() + "/" + filePath), yamlConfig!!)
            return reader.read(clazz)
        } catch (e: FileNotFoundException) {
            throw RuntimeException("Cloudopt Next Yaml: $filePath was not found!")
        } catch (e: YamlException) {
            throw RuntimeException("Cloudopt Next Yaml: $filePath cannot be converted to yaml!")
        }

        return null
    }

    /**
     * According to the class annotation above automatically read yaml and instantiated
     * @param clazz Class name
     * @return Object
     */
    fun read(clazz: KClass<*>): Any? {
        var annotation = clazz.java.getAnnotation<ConfigureBean>(ConfigureBean::class.java)

        if (annotation != null) {
            var map: Map<*, *>
            if (annotation?.prefix.isNotBlank()) {
                map = read(annotation!!.filePath, annotation!!.prefix) ?: HashMap<String, String>()
            } else {
                return read(annotation!!.filePath, clazz.java)
            }

            if (map?.isEmpty() ?: true) {
                throw RuntimeException("Cloudopt Next Yaml: " + annotation!!.filePath + "is empty, cannot be converted to yaml!")
            }

            return Maper.toObject(map as Map<String, Any>, clazz.java)


        } else {
            throw RuntimeException("Cloudopt Next Yaml: " + clazz.simpleName + "cannot be found @ConfigureBean!")
        }

    }

    /**
     * Get the project runtime path
     * @return Path
     */
    fun getRootClassPath(): String {
        if (rootPath == null || rootPath.equals("")) {
            try {
                val path = Yamler::class.java!!.getClassLoader().getResource("")!!.toURI().getPath()
                rootPath = File(path).getAbsolutePath()
            } catch (e: Exception) {
                val path = Yamler::class.java!!.getClassLoader().getResource("")!!.getPath()
                rootPath = File(path).getAbsolutePath()
            }

        }
        return rootPath
    }


}
