/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.internal.collector

import org.neo4j.cypher._

class DataCollectorStateAcceptanceTest extends ExecutionEngineFunSuite {

  import DataCollectorMatchers._

  private val IDLE = "collector is idle"
  private val COLLECTING = "collecting data"
  private val HAS_DATA = "collector has data"

  test("QUERIES: happy path collect cycle") {
    assertStatus(IDLE)

    execute("CALL db.stats.collect('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> true,
      "message" -> "Collection started."
    )

    assertStatus(COLLECTING)

    execute("CALL db.stats.stop('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> true,
      "message" -> "Collection stopped."
    )

    assertStatus(HAS_DATA)

    execute("CALL db.stats.clear('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> true,
      "message" -> "Data cleared."
    )

    assertStatus(IDLE)

    execute("CALL db.stats.collect('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> true,
      "message" -> "Collection started."
    )

    assertStatus(COLLECTING)
  }

  test("QUERIES: misuse while idle") {
    // when
    execute("CALL db.stats.stop('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> true,
      "message" -> "Collector is idle, no collection ongoing."
    )

    // then
    assertStatus(IDLE)

    // when
    execute("CALL db.stats.clear('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> true,
      "message" -> "Collector is idle and has no data."
    )

    // then
    assertStatus(IDLE)
  }

  test("QUERIES: misuse while collecting") {

    // given
    execute("CALL db.stats.collect('QUERIES')")
    assertStatus(COLLECTING)

    // when
    execute("CALL db.stats.collect('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> false,
      "message" -> "Collection is already started."
    )

    // then
    assertStatus(COLLECTING)

    // when
    execute("CALL db.stats.clear('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> true,
      "message" -> "Collection stopped and data cleared."
    )

    // then
    assertStatus(IDLE)
  }

  test("QUERIES: misuse while having data") {

    // given
    execute("CALL db.stats.collect('QUERIES')")
    execute("CALL db.stats.stop('QUERIES')")
    assertStatus(HAS_DATA)

    // when
    execute("CALL db.stats.collect('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> false,
      "message" -> "Collector already has data, clear data before collecting again."
    )

    // then
    assertStatus(HAS_DATA)

    // when
    execute("CALL db.stats.stop('QUERIES')").single should beMap(
      "section" -> "QUERIES",
      "success" -> true,
      "message" -> "Collector is already stopped and has data, no collection ongoing."
    )

    // then
    assertStatus(HAS_DATA)
  }

  private def assertStatus(status: String): Unit = {
    val res = execute("CALL db.stats.status()").single
    res should beMapContaining(
      "status" -> status,
      "section" -> "QUERIES"
    )
  }
}
