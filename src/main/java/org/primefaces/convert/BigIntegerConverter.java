/**
 * Copyright 2009-2019 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.convert;

import java.util.Map;

public class BigIntegerConverter extends javax.faces.convert.BigIntegerConverter implements ClientConverter {

    @Override
    public Map<String, Object> getMetadata() {
        return null;
    }

    @Override
    public String getConverterId() {
        return BigIntegerConverter.CONVERTER_ID;
    }
}
