/**
 * Copyright (c) 2025 the Eclipse FA³ST Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.digitaltwin.fa3st.service.messagebus;

import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.EventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.SubscriptionId;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.SubscriptionInfo;
import org.eclipse.digitaltwin.fa3st.service.config.Configurable;


/**
 * An implementation of an internal message bus inherits from this interface. This message bus is used for internal
 * events.
 *
 * @param <T> type of the corresponding configuration class
 */
public interface MessageBus<T extends MessageBusConfig> extends Configurable<T> {

    /**
     * Publish a new EventMessage to the message bus.
     *
     * @param message which should be published
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException if publish fails
     */
    public void publish(EventMessage message) throws MessageBusException;


    /**
     * Subscribe to event messages published in the message bus. The Subscription Info determines which event messages
     * are considered in detail.
     *
     * @param subscriptionInfo to determine which event messages should be considered
     * @return the id of the created subscription in the message bus. The id can be used to update/unsubscribe this
     *         subscription.
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException if subscribing fails
     */
    public SubscriptionId subscribe(SubscriptionInfo subscriptionInfo) throws MessageBusException;


    /**
     * Unsubscribe from a specific subscription by id.
     *
     * @param id of the subscription which should be deleted
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException if unsubscribing fails
     */
    public void unsubscribe(SubscriptionId id) throws MessageBusException;


    /**
     * Starts the MessageBus.
     *
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException if starting fails
     */
    public void start() throws MessageBusException;


    /**
     * Stops the MessageBus.
     */
    public void stop();

}
