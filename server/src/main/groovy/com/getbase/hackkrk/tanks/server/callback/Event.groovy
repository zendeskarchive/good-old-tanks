package com.getbase.hackkrk.tanks.server.callback

import groovy.transform.EqualsAndHashCode
import groovy.transform.Immutable
import groovy.transform.ToString
import lombok.AllArgsConstructor

import static lombok.AccessLevel.PACKAGE

@Immutable
@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode
@ToString(includePackage = false)
class Event<T> {

    String id

}
