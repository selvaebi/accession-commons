/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ampt2d.test.utils;

import uk.ac.ebi.ampt2d.accession.common.generators.ModelHashAccession;
import uk.ac.ebi.ampt2d.accession.sample.SampleModel;
import uk.ac.ebi.ampt2d.accession.sample.persistence.SampleEntity;
import uk.ac.ebi.ampt2d.accession.sample.rest.SampleDTO;
import uk.ac.ebi.ampt2d.accession.study.StudyModel;
import uk.ac.ebi.ampt2d.accession.study.persistence.StudyEntity;
import uk.ac.ebi.ampt2d.accession.study.rest.StudyDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TestHelper {

    public static HashMap<String, String> generateSampleMap(int value) {
        HashMap<String, String> sampleMap = new HashMap<>();
        sampleMap.put("title", "Title" + value);
        sampleMap.put("type", "Type" + value);
        sampleMap.put("submitterEmail", "Email" + value);
        return sampleMap;
    }

    public static SampleDTO generateSampleDTO(int value) {
        return new SampleDTO(generateSampleMap(value));
    }

    public static List<ModelHashAccession<SampleModel, String, String>> generateSampleModelHashAccessions(
            int... values) {
        return Arrays.stream(values).mapToObj(i -> ModelHashAccession.of(
                (SampleModel) new SampleDTO(generateSampleMap(i)), "hash" + i, "accession" + i))
                .collect(Collectors.toList());
    }

    public static SampleEntity generateSampleEntity(int value) {
        return new SampleEntity(generateSampleMap(value), "hash" + value, "accession" + value);
    }

    public static List<SampleEntity> generateSampleEntities(
            int... values) {
        return Arrays.stream(values).mapToObj(TestHelper::generateSampleEntity).collect(Collectors.toList());
    }

    public static HashMap<String, String> generateStudyMap(int value) {
        HashMap<String, String> studyMap = new HashMap<>();
        studyMap.put("title", "Title" + value);
        studyMap.put("type", "Type" + value);
        studyMap.put("submitterEmail", "Email" + value);
        return studyMap;
    }

    public static StudyEntity generateStudyEntity(int value) {
        return new StudyEntity(generateStudyMap(value), "accession" + value, "hash" + value);
    }

    public static List<StudyEntity> generateStudyEntities(int... values) {
        return Arrays.stream(values).mapToObj(TestHelper::generateStudyEntity).collect(Collectors.toList());
    }

    public static StudyDTO generateStudyDTO(int value){
        return new StudyDTO(generateStudyMap(value));
    }

    public static List<StudyDTO> generateStudyDTOs(int... values){
        return Arrays.stream(values).mapToObj(TestHelper::generateStudyDTO).collect(Collectors.toList());
    }

    public static List<ModelHashAccession<StudyModel, String, String>> generateStudyModelHashAccessions(int... values) {
        return Arrays.stream(values).mapToObj(i -> ModelHashAccession.of(
                (StudyModel) new StudyDTO(generateStudyMap(i)), "hash" + i, "accession" + i))
                .collect(Collectors.toList());
    }

}
