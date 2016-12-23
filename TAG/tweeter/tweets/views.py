from django.db.models import Q
from django.contrib.auth import login
from django.http import HttpResponse
from django.http import HttpResponseRedirect
from django.shortcuts import redirect, get_object_or_404, render
from django.urls import reverse
from django.utils.decorators import method_decorator
from django.views import generic

from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User

from .models import Tweet, Profile
from .forms import TweetForm, UserForm


class IndexView(generic.ListView):
    template_name = 'tweets/index.html'
    context_object_name = 'tweet_list'

    def get_queryset(self):
        return Tweet.objects.filter(
            Q(owner__profile__in=self.request.user.profile.followed.all())
            | Q(owner__profile=self.request.user.profile)
        ).\
            order_by('-date_published')[:10]


class UserListView(generic.ListView):
    template_name = 'tweets/user_list.html'
    context_object_name = 'user_list'

    def get_queryset(self):
        return User.objects.all()

    @method_decorator(login_required)
    def dispatch(self, request, *args, **kwargs):
        return super(UserListView, self).dispatch(request, *args, **kwargs)


class TweetView(generic.DetailView):
    model = Tweet
    template_name = 'tweets/tweet.html'


def stub_view(name):
    def view(request):
        return HttpResponse("You're in {name}".format(name=name), content_type="text/plain")

    return view


def user_view(request, pk):
    user = get_object_or_404(User, pk=pk)
    following = user.profile in request.user.profile.followed.all()
    print("Following value is {following}".format(following=following))
    return render(request, 'tweets/user.html', {'user': user, 'following': following})


def set_follow(start_following):
    @login_required
    def follow(request, pk):
        action = "follow" if start_following else "unfollow"
        print("Trying to {action} user with id {pk} logged as {username}".
              format(pk=pk, username=request.user.username, action=action))

        followed_user = get_object_or_404(Profile, pk=pk)
        if start_following:
            request.user.profile.followed.add(followed_user)
        else:
            request.user.profile.followed.remove(followed_user)

        return HttpResponseRedirect(reverse('tweets:user_detail', args=(pk,)))

    return follow


@login_required
def create_tweet(request):
    if request.method == 'POST':
        form = TweetForm(request.POST)
        if form.is_valid():
            Tweet.objects.create(owner=request.user, text=form.cleaned_data.get('text'))
            return redirect('tweets:index')

    else:
        form = TweetForm()

    return render(request, 'tweets/create_tweet.html', {'form': form})


def signup(request):
    if request.method == "POST":
        form = UserForm(request.POST)
        if form.is_valid():
            new_user = User.objects.create_user(**form.cleaned_data)
            new_user.save()
            Profile.objects.create(user=new_user)

            login(user=new_user, request=request)
            # redirect, or however you want to get to the main view
            return redirect('tweets:index')
    else:
        form = UserForm()

    return render(request, 'tweets/signup.html', {'form': form, })


