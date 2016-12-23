from __future__ import unicode_literals

from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.dispatch import receiver


class Tweet(models.Model):
    from django.utils import timezone

    owner = models.ForeignKey(User, on_delete=models.CASCADE)
    text = models.CharField(max_length=140)
    date_published = models.DateTimeField('date_published', default=timezone.now)

    def __unicode__(self):
        return self.text


class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    followed = models.ManyToManyField("self", symmetrical=False)

    def __unicode__(self):
        result = "{user} following: ".format(user=self.user)
        for followed in self.followed.all():
            result = result + ", " + followed.user.username
        return result