/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */
package com.kinvey.android.callback;

import com.kinvey.java.core.KinveyClientCallback;

/**
 * This callback is used for User Management operations, such as sending emails or password reset forms.
 * <p>
 * This methods which use this callback do not provide any return values, instead they either "ran" or "didn't run".
 * </p>
 *
 *
 * @author mjsalinger
 * @since 2.0
 */
public interface KinveyUserManagementCallback extends KinveyClientCallback<Void> {

    /**
     * Method invoked when a user operation completes.
     *
     * @param result - typed to {@code Void} because there is no usable return value.
     */
    @Override
    public void onSuccess(Void result);

    /**
     * Method invoked when a user operation fails to complete.
     * @param error - details about the error.
     */
    @Override
    public void onFailure(Throwable error);
}
