# Sakany - Project Abstraction

> **NOTE:** Fill this file with the full project description from Chapter 1 and Chapter 2.

<!-- 
Paste the full content here:
- Chapter 1: Introduction (Overview, Key Features, Problem Statement, etc.)
- Chapter 2: Theoretical Background and Tools
-->
Chapter 1: Introduction
Overview
In large residential areas, management often struggles with communication
between residents and administrators. Issues such as delays in fixing maintenance
problems, disorganized visitor entry, and no clear system for complaints,
announcements or community events are common. Furthermore, reliance on
manual procedures for payments and service requests leads to inefficiency, a lack
of transparency, and low resident satisfaction.
This project aims to build a digital platform that improves facility
management and enhances residents' experience. The system focuses on
delivering fast, easy-to-access services like maintenance handling, visitor access
control, and electronic payments, while creating an effective communication
channel between residents and administrators.
Key Features
The system provides comprehensive features for both residents and
administrators:
For Residents (Mobile App):
● Maintenance Requests: Submit private or public maintenance requests
with real-time tracking
● Visitor Management: Generate QR codes for visitors and delivery staff
to ensure secure gate access
● Community Alerts: Report missing people, pets, or items with alerts
shared quickly across the community
● Feedback System: Send complaints and suggestions with public voting
on shared ideas
● Events: View and join community events
● Payments: Manage monthly fees and secure online payments through an
integrated gateway
For Administrators (Web Dashboard):
● Monitor all operations efficiently through statistics and insights
● Track and manage maintenance requests
● Oversee visitor access logs
● Review community feedback and voting
● Manage events and announcements
● Handle payment records and financial transactions
Problem Statement
Residential compounds face significant operational challenges that negatively
impact resident satisfaction and administrative efficiency:
1. 2. 3. 4. 5. Communication Gaps: Ineffective communication channels between
residents and management lead to unresolved issues and frustration
Maintenance Delays: Slow response times for maintenance requests due
to disorganized tracking systems
Security Concerns: Lack of proper visitor verification and tracking
systems at entry points
Manual Processes: Heavy reliance on paper-based procedures for service
requests and payments
Limited Transparency: Residents have no visibility into the status of
their requests or complaints
6. 7. 8. Disorganized Events: No centralized platform for community
announcements and event coordination
Payment Inefficiencies: Traditional payment methods create delays and
lack proper documentation
Community Disconnection: No platform for residents to interact, share
concerns, or participate in collective decisions
Problem Solution
The proposed digital platform addresses these challenges through a
comprehensive two-component system:
1. Mobile Application for Residents
A user-friendly mobile app that enables residents to:
● Submit and track maintenance requests in real-time
● Generate secure QR codes for visitor and delivery access
● Report and receive alerts about missing items, pets, or people
● Submit complaints and vote on community suggestions
● Stay informed about community events
● Make secure online payments for fees and services
2. Web Dashboard for Administrators
A powerful administrative interface that allows management to:
● Monitor all system operations through real-time statistics and insights
● Efficiently manage and respond to maintenance requests
● Control and track visitor access
● Review community feedback and implement popular suggestions
● Organize and promote community events
● Process and track all financial transactions
● Generate reports for better decision-making
This integrated solution replaces manual processes with automated, transparent
workflows, ensuring faster service delivery, enhanced security, and improved
community engagement.
Purpose
The primary purpose of this system is to:
1. 2. 3. 4. 5. 6. 7. 8. Enhance Operational Efficiency: Streamline compound management
operations and reduce manual workload
Improve Service Quality: Enable faster response times to resident
requests and issues
Strengthen Security: Implement secure visitor verification and access
control systems
Increase Transparency: Provide clear visibility into request status and
administrative processes
Foster Community Engagement: Create a platform for residents to
interact, share ideas, and participate in community decisions
Modernize Financial Management: Implement secure, traceable
electronic payment systems
Boost Resident Satisfaction: Deliver convenient, accessible services that
improve quality of life
Enable Data-Driven Decisions: Provide administrators with insights and
analytics for better management
Scope
In Scope:
Resident Features:
● Maintenance request submission and tracking (private and public)
● QR code generation for visitors and deliveries
● Missing items/pets/people reporting system
● Complaints and suggestions with voting mechanism
● Community events viewing and participation
● Online payment integration for fees and services
● Push notifications for updates and alerts
Administrative Features:
● Comprehensive dashboard with statistics and analytics
● Maintenance request management and assignment
● Visitor access control and logs
● Feedback review and response system
● Event creation and management
● Payment processing and financial reporting
● User management and access control
Technical Implementation:
● Mobile application development (iOS and Android)
● Web-based administrative dashboard
● Secure backend API and database
● Payment gateway integration
● QR code generation and scanning system
● Real-time notifications system
● Cloud-based hosting and data storage
Out of Scope:
● Physical gate hardware installation
● Smart home integration features
● Third-party vendor management systems
● Legal document management
● Building construction management
● Utility billing integration
● Property sales or rental listings
Expected Benefits
Operational Benefits:
● Significant reduction in manual administrative work
● Faster service response times
● Improved resource allocation and tracking
● Better operational insights through data analytics
Security Benefits:
● Enhanced security at entry points through QR verification
● Complete visitor tracking and logs
● Reduced unauthorized access incidents
Financial Benefits:
● Streamlined payment collection process
● Reduced payment delays and disputes
● Better financial tracking and reporting
● Lower operational costs
Community Benefits:
● Stronger resident-administrator communication
● Increased community engagement and participation
● More transparent decision-making processes
● Higher resident satisfaction and quality of life
Environmental Benefits:
● Reduced paper usage through digitalization
● Lower carbon footprint from optimized operations
Conclusion
This system is expected to boost overall efficiency in compound
operations, cut down on manual work, and speed up service responses. It also
improves security at entry points, strengthens community interaction, and
provides a more transparent and reliable financial process. Ultimately, it
contributes to a better-managed and more comfortable living environment for
residents.
Chapter 2: Theoretical Background and Tools
2.1 Introduction
The concept of residential living has evolved significantly in recent years.
Modern residential compounds are no longer just collections of apartment
buildings; they are self-contained communities that require efficient management
of resources, security, and social interactions. This chapter outlines the theoretical
foundation of the Residential Compound Management System. It provides a
comprehensive analysis of existing solutions currently used in the market,
distinguishing between manual methods and enterprise software. Furthermore, it
details the specific functional modules of our proposed system, highlighting the
gap it fills in the current landscape. Finally, this chapter provides an in-depth
technical overview of the development tools—React, React Native, TypeScript,
Spring Boot, PostgreSQL, and Figma—explaining the rationale behind their
selection for this specific project.
2.2 Related Work
Managing the daily operations of a residential compound is a multi-faceted
challenge involving the coordination of residents, security personnel,
maintenance technicians, and administrative staff. To understand the necessity of
our proposed system, it is essential to analyze the two primary methods currently
employed in the industry:
2.2.1 Manual and Fragmented Digital Methods
A significant number of residential compounds, particularly mid-sized
ones, rely on a mix of manual processes and disconnected digital tools.
● Communication: Interactions often occur through informal channels like
WhatsApp or Facebook groups. While accessible, these platforms lack
structure. Important announcements get lost in chat history, and residents
often feel their privacy is compromised by sharing phone numbers with the
entire community.
● Security: Visitor logs are frequently kept in paper notebooks at security
gates. This method is slow, prone to human error, and makes it nearly
impossible to search for historical entry data in the event of a security
incident.
● Maintenance: Requests are often made via phone calls to the
administration office. This leads to a lack of accountability; requests can
be forgotten, and residents have no visibility into whether a technician has
been assigned or when the work will be completed.
2.2.2 Enterprise Property Management Software
On the other end of the spectrum are robust Enterprise Resource Planning (ERP)
systems designed for real estate, such as Yardi, AppFolio, or Buildium.
● Focus on Finance: These systems are primarily designed for property
owners and landlords. Their core features revolve around lease
management, rent collection, and complex accounting.
● Complexity and Cost: These solutions are often prohibitively expensive
and require significant training to use. For a standard resident who simply
wants to report a broken streetlamp or book the clubhouse, the user
interface is often clunky, unintuitive, and overwhelming.
● Lack of Community: Most enterprise software treats the resident merely
as a tenant or a source of revenue, lacking features that foster community
spirit, such as event organization or social voting.
Our project bridges this gap by offering a solution that combines the professional
oversight of enterprise software with the ease of use and social connectivity found
in modern consumer applications.
2.3 Application Features and Differences
2.3.1 Detailed System Features
The proposed system is architected around eight core modules, each designed to
digitize specific workflows within the compound:
1. Maintenance Management:
This module transforms the chaotic process of fixing issues into a
streamlined workflow. Residents can submit requests for private issues
(e.g., plumbing leaks) or public hazards (e.g., broken elevator). The
system allows for categorizing urgency and uploading photos of the
damage. Admins receive these tickets and assign them to available
technicians. The resident is then kept in the loop through real-time status
updates, moving from "Submitted" to "In Progress" and finally
"Resolved."
2. QR Code Access Control:
Security is modernized through a digital gate pass system. Residents
expecting guests or food deliveries can generate a unique QR code via
their app. They can set this code for a single use or a specific time
window. When the visitor arrives, security personnel simply scan the
code using their dedicated interface. This verifies the entry instantly and
logs the timestamp, creating a secure, searchable digital visitor log.
3. Missing Items Reporting:
To enhance community support, this feature acts as a rapid alert system.
If a resident loses a pet, a set of keys, or even reports a suspicious vehicle,
they can file a report. Unlike a physical notice board that few people
check, this digital report can trigger notifications to other residents,
drastically increasing the chances of recovery and awareness.
4. Feedback and Voting:
This feature empowers residents to have a say in their living environment.
Private complaints ensure confidentiality for sensitive matters. However,
the unique "Public Suggestion" feature allows residents to propose
improvements, such as adding new gym equipment or landscaping
changes. The community can vote "Yes" or "No" on these ideas,
providing the administration with clear data on what the residents actually
want before they spend money.
5. Community Events:
Fostering a sense of belonging is a key goal of this project. Residents can
propose social events, such as weekend markets, yoga sessions, or chess
tournaments. Once an admin reviews and approves the proposal, it is
published to the community calendar. Residents can view details and
track upcoming activities, turning the compound from a place to sleep
into a place to live.
6. Payments and Billing:
Financial friction is reduced by integrating payment gateways directly
into the application. Residents can view their outstanding balances for
management fees or specific maintenance invoices. The system provides
a secure method to settle these bills instantly, automatically updating the
user's financial status and providing digital receipts for record-keeping.
7. Announcements:
Communication from the administration is centralized here. Whether it is
a critical alert about a water shut-off or a celebratory message for a
holiday, admins can push announcements to all users. This ensures that
official news is distinct from community chatter and that every resident
receives important information simultaneously.
8. Admin Dashboard:
The brain of the system is the Admin Dashboard. This web-based
interface gives management a high-level view of the compound’s health.
It includes visual analytics, such as charts showing the volume of
maintenance requests per month, the status of collected payments, and
user engagement levels. It also provides user management capabilities to
approve new resident accounts and manage technician profiles.
2.3.2 Key Differences and Value Proposition
Our solution differentiates itself through a philosophy of "Community-First
Management."
● Integrated Security Logic: Unlike standard apps that rely on a security
guard manually checking a list, our system enforces validation logic
through the database. A QR code generated by a resident is
cryptographically tied to their account, ensuring that access cannot be
faked.
● Democratization of Management: By allowing residents to vote on
suggestions and propose events, the system shifts the dynamic from a top-
down dictatorship to a collaborative community. This leads to higher
resident satisfaction and retention.
● Usability and Accessibility: We prioritize a "zero-training" interface. The
design philosophy ensures that a non-technical user can figure out how to
pay a bill or generate a QR code within seconds, removing the friction that
plagues older legacy software.
2.4 Tools and Technologies
The selection of the technology stack was driven by the need for scalability,
security, and a high-quality user experience.
2.4.1 UX/UI Design: Figma
Figma was utilized as the primary design tool before any development began. Its
vector-based interface allowed for the creation of precise wireframes and
interactive high-fidelity prototypes. This phase was crucial for testing user
flows—ensuring that buttons were placed logically and that the color scheme was
accessible. Figma’s collaboration features also allowed for rapid iteration of the
design based on hypothetical user feedback.
2.4.2 Frontend and Mobile: React & React Native with
TypeScript
To deliver a seamless experience for all users, we employed a dual-platform
strategy utilizing React for the web-based Admin Dashboard and React Native
for the resident’s mobile application. Furthermore, the entire frontend codebase
was developed using TypeScript rather than standard JavaScript.
● React (Web): Used for the Admin Dashboard, React’s component-based
architecture allows for the creation of complex data visualization tools and
management tables that update efficiently via the Virtual DOM.
● React Native (Mobile): Used for the Resident App, React Native allows
us to maintain a single codebase that deploys to both iOS and Android.
Crucially, it renders native UI components, ensuring the app feels smooth
and responsive, which is vital for features like QR code generation.
● TypeScript Benefits: By enforcing static typing, TypeScript significantly
improves code quality and reliability. It allows us to catch errors at
compile-time rather than runtime—for example, preventing a developer
from accidentally passing a text string into a mathematical calculation for
billing. This type safety creates a self-documenting codebase, making it
easier for the development team to collaborate and scale the application
without introducing bugs.
2.4.3 Backend: Java Spring Boot
The server-side logic is powered by Java with the Spring Boot framework. This
choice was made to ensure the system is enterprise-grade and secure.
● Robust Security: Spring Security is a powerful framework that handles
authentication and authorization. It allows us to easily implement Role-
Based Access Control (RBAC), ensuring that a "Resident" cannot access
the "Admin" dashboard.
● Scalability: Spring Boot is designed to build stand-alone, production-
grade applications. It handles RESTful API requests efficiently, managing
the traffic between the user’s mobile device and the database.
● Maintainability: Java’s strong typing and Spring’s dependency injection
make the codebase easier to maintain and debug as the application grows
in complexity.
2.4.4 Database: PostgreSQL
For the persistence layer, PostgreSQL was chosen over other options like MySQL
or MongoDB.
● Data Integrity: As an Object-Relational Database Management System
(ORDBMS), PostgreSQL is known for its strict adherence to SQL
standards and reliability. It supports complex relationships, such as linking
a "Maintenance Request" to a "User" and a "Technician" simultaneously.
● Advanced Features: It offers features like JSONB support, allowing for
flexibility if we need to store unstructured data in the future.
● ACID Compliance: PostgreSQL ensures that financial transactions (like
paying monthly fees) are processed reliably, preventing data corruption or
partial updates.