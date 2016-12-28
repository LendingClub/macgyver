/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core.reactor;

import org.slf4j.LoggerFactory;

import io.reactivex.functions.Consumer;



public class Consumers {

	
	public static <T extends Consumer<T>> T exceptionSafeConsumer(T consumer) {
		
		io.reactivex.functions.Consumer<T> x = new Consumer() {

			@Override
			public void accept(Object t) {
				try {
					consumer.accept((T)t);
				}
				catch (Exception e) {
					LoggerFactory.getLogger(consumer.getClass()).warn("uncaught exception",e);
				}
				
			}
		};
		return (T) x;
	}
}
