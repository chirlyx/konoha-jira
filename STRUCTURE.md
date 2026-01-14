Mission
• id (Long)
• title (String)
• description (String)
• rank (MissionRank)
• status (MissionStatus)
• rewardExperience (int)
• assignedTo (Ninja)
• createdAt (Instant)
• updatedAt (Instant)
• version (Long)

Ninja
• id (Long)
• username (String)
• password (String)
• firstName (String)
• lastName (String)
• village (String)
• role (Role)
• rank (Rank)
• experience (long)
• active (boolean)
• createdAt (Instant)
• updatedAt (Instant)
• version (Long)

Controllers
AuthController
• POST /api/auth/signup
• POST /api/auth/login

NinjaController
• GET /api/ninja/profile
• GET /api/ninja/missions
• GET /api/missions/available
• PUT /api/missions/{id}/assign
• PUT /api/missions/{id}/complete
• GET /api/ninja/stats

HokageController
• POST /api/hokage/missions
• GET /api/hokage/missions/pending
• PUT /api/hokage/missions/{id}/approve
• PUT /api/hokage/missions/{id}/fail
• PUT /api/hokage/missions/{id}/abort
• GET /api/hokage/analytics
• PUT /api/hokage/ninja/{id}/deactivate

