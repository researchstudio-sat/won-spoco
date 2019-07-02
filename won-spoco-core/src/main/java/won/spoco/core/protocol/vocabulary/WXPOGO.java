/*
 * Copyright 2012 Research Studios Austria Forschungsges.m.b.H. Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package won.spoco.core.protocol.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * WoN Vocabulary Extension POGO (Pokemon Go)
 */
public class WXPOGO {
    public static final String BASE_URI = "https://w3id.org/won/core#"; //TODO: change to "https://w3id.org/won/ext/pogo#" once won.js and pokemon.js was updated
    public static final String DEFAULT_PREFIX = "won"; //TODO: change to "wx-pogo"
    private static Model m = ModelFactory.createDefaultModel();

    public static final Property gymEx = m.createProperty(BASE_URI, "gymex");
    public static final Property raid = m.createProperty(BASE_URI, "raid");
    public static final Property level = m.createProperty(BASE_URI, "level");
    public static final Property pokemonId = m.createProperty(BASE_URI, "pokemonid");
    public static final Property pokemonForm = m.createProperty(BASE_URI, "pokemonform");

    public static final Resource PokemonGo = m.createResource("http://dbpedia.org/resource/Pokemon_Go");

    /**
     * Returns the base URI for this schema.
     *
     * @return the URI for this schema
     */
    public static String getURI() {
        return BASE_URI;
    }
}
