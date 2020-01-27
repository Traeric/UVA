# Generated by Django 2.2.4 on 2020-01-21 05:06

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('userManage', '0004_user_key_words'),
    ]

    operations = [
        migrations.AddField(
            model_name='voiceassistantkeyword',
            name='feedback',
            field=models.CharField(default='好的', max_length=32, verbose_name='语音助手反馈的语音'),
        ),
        migrations.AlterField(
            model_name='user',
            name='key_words',
            field=models.CharField(default='', max_length=128, verbose_name='当前用户拥有的关键字id，以逗号为间隔'),
        ),
    ]