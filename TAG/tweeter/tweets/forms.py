from django import forms

from django.contrib.auth.models import User

from .models import Tweet


class UserForm(forms.ModelForm):
    class Meta:
        model = User
        fields = ('username', 'email', 'password', )
        widgets = {
            'password': forms.PasswordInput(),
        }


class TweetForm(forms.ModelForm):
    class Meta:
        model = Tweet
        fields = ['text']

