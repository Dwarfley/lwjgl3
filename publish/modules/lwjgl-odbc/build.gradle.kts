/*
* Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
plugins {
    id("lwjgl-module")
}

lwjglPublication {
    title("LWJGL - ODBC bindings")
    description("A C programming language interface that makes it possible for applications to access data from a variety of database management systems (DBMSs).")
    platforms {
        platform(ALL, NATIVE_OPTIONAL)
    }
}