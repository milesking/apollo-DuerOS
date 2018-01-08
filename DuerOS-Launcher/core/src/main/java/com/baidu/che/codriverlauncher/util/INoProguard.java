/******************************************************************************
 * Copyright 2017 The Baidu Authors. All Rights Reserved.
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
 *****************************************************************************/
package com.baidu.che.codriverlauncher.util;

import java.io.Serializable;

/*
 * No ProGuard for class who implements me.
 * Do not forget give settings of "proguard-project.txt":
 * -keep interface {@link INoProguard} {*;}
 * -keep interface * extends {@link INoProguard} {*;}
 * -keep class * implements {@link INoProguard} {*;}
 * -keepclasseswithmembernames class * implements {@link INoProguard} {*;}
 */

public interface INoProguard extends Serializable {

}
