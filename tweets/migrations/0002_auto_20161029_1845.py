# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2016-10-29 18:45
from __future__ import unicode_literals

import datetime
from django.db import migrations, models
from django.utils.timezone import utc


class Migration(migrations.Migration):

    dependencies = [
        ('tweets', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='tweet',
            name='date_published',
            field=models.DateTimeField(default=datetime.datetime(2016, 10, 29, 18, 45, 56, 439657, tzinfo=utc), verbose_name='date_published'),
        ),
    ]
