/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.graphdb.factory;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import org.neo4j.kernel.DummyExtensionFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.neo4j.helpers.collection.Iterables.count;

class DatabaseManagementServiceBuilderStateTest
{
    @Test
    void mustBeAbleToRemoveAddedExtensions()
    {
        DummyExtensionFactory extensionFactory = new DummyExtensionFactory();
        GraphDatabaseFactoryState state = new GraphDatabaseFactoryState();
        long initialCount = count( state.getExtension() );

        state.addExtensions( Collections.singleton( extensionFactory ) );
        assertThat( count( state.getExtension() ), is( initialCount + 1 ) );

        state.removeExtensions( e -> e == extensionFactory );
        assertThat( count( state.getExtension() ), is( initialCount ) );
    }
}