import { StreamChat } from 'stream-chat';
import { Channel, ChannelHeader, Chat, MessageInput, MessageList, Thread, Window } from 'stream-chat-react';

import 'stream-chat-react/dist/css/index.css';

const chatClient = StreamChat.getInstance('dc92fmup6nww');
const userToken =
    'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoicmVzdGxlc3MtY2VsbC01In0.CjtexZiPosRUnP254Os7WX57Yt-OZVOkHr7zkssrApA';

chatClient.connectUser(
    {
        id: 'restless-cell-5',
        name: 'restless-cell-5',
        image: 'https://getstream.io/random_png/?id=restless-cell-5&name=restless-cell-5'
    },
    userToken
);

const channel = chatClient.channel('messaging', 'custom_channel_id', {
    // add as many custom fields as you'd like
    image: 'https://www.drupal.org/files/project-images/react.png',
    name: 'Talk about React',
    members: ['restless-cell-5']
});

export const CaseChat = () => (
    <Chat client={chatClient} theme="messaging light">
        <Channel channel={channel}>
            <Window>
                <ChannelHeader />
                <MessageList />
                <MessageInput />
            </Window>
            <Thread />
        </Channel>
    </Chat>
);
