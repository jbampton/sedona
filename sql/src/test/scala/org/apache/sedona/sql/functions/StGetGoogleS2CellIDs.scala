/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sedona.sql.functions

import org.apache.sedona.sql.{GeometrySample, TestBaseScala}
import org.apache.spark.sql.functions.{col, expr, lit, when}
import org.scalatest.{GivenWhenThen, Matchers}

class StGetGoogleS2CellIDs extends TestBaseScala with Matchers with GeometrySample with GivenWhenThen {
  import sparkSession.implicits._

  describe("should pass ST_GetGoogleS2CellIDs"){

    it("should return null while using ST_GetGoogleS2CellIDs when geometry is empty") {
      Given("DataFrame with null line strings")
      val geometryTable = sparkSession.sparkContext.parallelize(1 to 10).toDF()
        .withColumn("geom", lit(null))

      When("using ST_MakePolygon on null geometries")
      val geometryTableWithCellIDs = geometryTable
        .withColumn("cell_ids", expr("ST_GetGoogleS2CellIDs(geom)"))


      Then("no exception should be raised")
    }

    it("should correctly return array of cell ids use of ST_GetGoogleS2CellIDs"){
      Given("DataFrame with valid line strings")
      val geometryTable = Seq(
        "POINT(1 2)",
        "LINESTRING(-5 8, -6 1, -8 6, -2 5, -6 1, -5 8)",
        "POLYGON ((75 29, 77 29, 77 29, 75 29))"

      ).map(geom => Tuple1(wktReader.read(geom))).toDF("geom")

      When("using ST_MakePolygon on those geometries")
      val geometryDfWithCellIDs = geometryTable
        .withColumn("cell_ids", expr("ST_GetGoogleS2CellIDs(geom)"))

      Then("valid should have cell ids returned")
      geometryDfWithCellIDs.selectExpr("concat_ws(',', cast(cell_ids as array<string>))")
        .collect().map(r => r.get(0)) should contain theSameElementsAs Seq(
        "4611686018427387905",
        "4611686018427387905,4611686018427387905,4611686018427387905,4611686018427387905,4611686018427387905,4611686018427387905",
        "4611686018427387905,4611686018427387905,4611686018427387905,4611686018427387905"
      )

    }
  }
}
