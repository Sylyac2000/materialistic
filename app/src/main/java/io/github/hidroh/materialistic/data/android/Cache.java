/*
 * Copyright (c) 2017 Ha Duy Trung
 *
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

package io.github.hidroh.materialistic.data.android;

import android.content.Context;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.github.hidroh.materialistic.data.LocalCache;
import io.github.hidroh.materialistic.data.MaterialisticDatabase;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class Cache implements LocalCache {
    private final MaterialisticDatabase mDatabase;
    private final MaterialisticDatabase.SavedStoriesDao mSavedStoriesDao;
    private final MaterialisticDatabase.ReadStoriesDao mReadStoriesDao;
    private final MaterialisticDatabase.ReadableDao mReadableDao;

    @Inject
    public Cache(Context context,
                 MaterialisticDatabase.SavedStoriesDao savedStoriesDao,
                 MaterialisticDatabase.ReadStoriesDao readStoriesDao,
                 MaterialisticDatabase.ReadableDao readableDao) {
        mDatabase = MaterialisticDatabase.getInstance(context);
        mSavedStoriesDao = savedStoriesDao;
        mReadStoriesDao = readStoriesDao;
        mReadableDao = readableDao;
    }

    @Nullable
    @Override
    public String getReadability(String itemId) {
        MaterialisticDatabase.Readable readable = mReadableDao.selectByItemId(itemId);
        return readable != null ? readable.getContent() : null;
    }

    @Override
    public void putReadability(String itemId, String content) {
        mReadableDao.insert(new MaterialisticDatabase.Readable(itemId, content));
    }

    @Override
    public boolean isViewed(String itemId) {
        return mReadStoriesDao.selectByItemId(itemId) != null;
    }

    @Override
    public void setViewed(String itemId) {
        mReadStoriesDao.insert(new MaterialisticDatabase.ReadStory(itemId));
        Observable.just(itemId)
                .map(id -> MaterialisticDatabase.URI_READ.buildUpon().appendPath(id).build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> mDatabase.setLiveValue(uri));
    }

    @Override
    public boolean isFavorite(String itemId) {
        return mSavedStoriesDao.selectByItemId(itemId) != null;
    }
}
