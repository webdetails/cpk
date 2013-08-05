/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.elements;

/**
 *
 * ElementInfo has the relevant information to be passed to the content string.
 *
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public class ElementInfo {

    private boolean hasCache = false;
    private int cacheDuration;
    private String mimeType;
    private String attachmentName;

    public ElementInfo() {
    }

    public ElementInfo(String mimeType) {
        this.mimeType = mimeType;
    }

    public ElementInfo(String mimeType, int cacheDuration) {
        
        this.cacheDuration = cacheDuration;
        this.mimeType = mimeType;
        
        // if cacheDuration > 0, hasCache is true
        setHasCache(cacheDuration>0);
        
    }

    public boolean isHasCache() {
        return hasCache;
    }

    public void setHasCache(boolean hasCache) {
        this.hasCache = hasCache;
    }

    public int getCacheDuration() {
        return cacheDuration;
    }

    public void setCacheDuration(int cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean hasAttachment() {
        return getAttachmentName() != null && !getAttachmentName().equals("");
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }
}
