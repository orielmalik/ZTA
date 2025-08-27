import collections

if not hasattr(collections, "Mapping"):
    import collections.abc

    collections.Mapping = collections.abc.Mapping

import yaml
from experta import *


class AccessEngine(KnowledgeEngine):
    acl = {}
    decision = None
    reason = None

    def __init__(self):
        super().__init__()
        self.decision = None
        self.reason = None

    # Rule 1: ADMIN users have access to everything
    @Rule(Fact(role1="ADMIN"))
    def admin_allow_all(self):
        """ADMIN role overrides all restrictions"""
        self.decision = True
        self.reason = "ADMIN_OVERRIDE - Full access granted"
        self.halt()

    # Rule 2: YOUNG users are denied specific resources
    @Rule(
        Fact(role2="YOUNG"),
        Fact(resource=MATCH.r),
        TEST(lambda r: r in AccessEngine.acl.get("YOUNG_DENY", []))
    )
    def young_user_deny(self, r):
        """YOUNG users denied access to restricted resources"""
        self.decision = False
        self.reason = f"YOUNG_DENY - {r} not allowed for young users"
        self.halt()

    # Rule 3: REMOTE users are denied specific resources
    @Rule(
        Fact(role3="REMOTE"),
        Fact(resource=MATCH.r),
        TEST(lambda r: r in AccessEngine.acl.get("REMOTE_DENY", []))
    )
    def remote_user_deny(self, r):
        """REMOTE users denied access to location-restricted resources"""
        self.decision = False
        self.reason = f"REMOTE_DENY - {r} not available for remote users"
        self.halt()

    # Rule 4: ADMIN-only resources
    @Rule(
        Fact(role1="USER"),  # Non-admin user
        Fact(resource=MATCH.r),
        TEST(lambda r: r in AccessEngine.acl.get("ADMIN_ONLY", []))
    )
    def admin_only_deny(self, r):
        """Certain resources require ADMIN privileges"""
        self.decision = False
        self.reason = f"ADMIN_ONLY - {r} requires administrator privileges"
        self.halt()

    # Rule 5: Default allow for valid resources
    @Rule(
        Fact(resource=MATCH.r),
        TEST(lambda r: r in AccessEngine.acl.get("ALLOWED_RESOURCES", []))
    )
    def allow_valid_resource(self, r):
        """Allow access to valid resources if no restrictions apply"""
        self.decision = True
        self.reason = f"ALLOWED - {r} access granted"
        self.halt()

    # Rule 6: Deny access to unknown/invalid resources
    @Rule(Fact(resource=MATCH.r))
    def deny_unknown_resource(self, r):
        """Deny access to resources not in allowed list"""
        self.decision = False
        self.reason = f"UNKNOWN_RESOURCE - {r} is not a valid resource"
        self.halt()


def check_access(resource, rules:list[str], acl_rules):
    AccessEngine.acl = acl_rules
    engine = AccessEngine()
    engine.reset()

    # Declare facts
    engine.declare(Fact(resource=resource))
    engine.declare(Fact(role1=rules[0]))
    engine.declare(Fact(role2=rules[1]))
    engine.declare(Fact(role3=rules[2]))

    # Run the expert system
    engine.run()

    return {
        "allow": engine.decision,
        "reason": engine.reason,
    }


def load_acl_config(file_path="acl.yaml"):
    try:
        with open(file_path, "r") as f:
            return yaml.safe_load(f)
    except FileNotFoundError:
        raise





