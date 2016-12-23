from django.conf.urls import url
from django.contrib.auth import views as auth_views
from django.contrib.auth.decorators import login_required

from . import views

app_name = 'tweets'
urlpatterns = [
    url(r'^$', login_required(views.IndexView.as_view()), name='index'),

    url(r'^login/$', auth_views.login,
        {'template_name': 'tweets/login.html'}, name='login'),

    url(r'logout/$', auth_views.logout,
        {'template_name': 'tweets/logout.html'}, name='logout'),

    url(r'signup/$', views.signup, name='signup'),

    url(r'^user/all/$', views.UserListView.as_view(), name='user_list'),
    url(r'^user/(?P<pk>[0-9]+)/$', views.user_view, name='user_detail'),

    # TODO implement view
    url(r'^user/me/$', login_required(views.stub_view('user_me')), name='user_me'),

    # TODO implement form
    # TODO implement view
    url(r'^user/me/edit/$', login_required(views.stub_view('user_me_edit')), name='user_me_edit'),

    url(r'^user/(?P<pk>[0-9]+)/follow/$', views.set_follow(True), name='user_follow'),
    url(r'^user/(?P<pk>[0-9]+)/unfollow/$', views.set_follow(False), name='user_unfollow'),

    url(r'^tweet/$', views.create_tweet, name='create_tweet'),
    url(r'^tweet/(?P<pk>[0-9]+)/$', views.TweetView.as_view(), name='tweet'),
]
