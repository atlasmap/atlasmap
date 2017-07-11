/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.ipaas.itests;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

import java.util.Date;

public class MockUser implements User {

	private static final long serialVersionUID = -7980625793724945814L;
	private String name;
	private String screenName;
	
	@Override
	public int compareTo(User o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAccessLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	@Override
	public String getScreenName() {
		return this.screenName;
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isContributorsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBiggerProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMiniProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOriginalProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBiggerProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMiniProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOriginalProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDefaultProfileImage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isProtected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getFollowersCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileBackgroundColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileTextColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileLinkColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileSidebarFillColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileSidebarBorderColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isProfileUseBackgroundImage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDefaultProfile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShowAllInlineMedia() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getFriendsCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Date getCreatedAt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFavouritesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getUtcOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTimeZone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileBackgroundImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileBackgroundImageUrlHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileBannerURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileBannerRetinaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileBannerIPadURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileBannerIPadRetinaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileBannerMobileURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProfileBannerMobileRetinaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isProfileBackgroundTiled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLang() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStatusesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isGeoEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVerified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTranslator() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getListedCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFollowRequestSent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URLEntity[] getDescriptionURLEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URLEntity getURLEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getWithheldInCountries() {
		// TODO Auto-generated method stub
		return null;
	}

}
