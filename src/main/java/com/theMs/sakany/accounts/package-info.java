/**
 * Accounts Module - User and Profile Management
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
    "shared",
    "shared :: cqrs",
    "shared :: domain",
    "shared :: exception",
    "shared :: jpa"
})
package com.theMs.sakany.accounts;
