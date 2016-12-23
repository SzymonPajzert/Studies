from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from django.contrib.auth.models import User

from models import Tweet, Profile

admin.site.register(Tweet)


class FollowerInLine(admin.StackedInline):
    model = Profile
    can_delete = False  # TODO - check why

    verbose_name = "Follower"
    verbose_name_plural = "Followers"


class UserAdmin(BaseUserAdmin):
    inlines = (FollowerInLine, )

admin.site.unregister(User)
admin.site.register(User, UserAdmin)
