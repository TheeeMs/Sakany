/**
 * Community Module - Alerts, Feedback, and Announcements
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
    "shared",
    "shared :: cqrs",
    "shared :: domain",
    "shared :: exception",
    "shared :: jpa"
})
package com.theMs.sakany.community;
